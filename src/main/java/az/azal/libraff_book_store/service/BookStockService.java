package az.azal.libraff_book_store.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.entity.BookStockEntity;
import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.enums.TransactionType;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.repository.BookStockRepository;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import az.azal.libraff_book_store.response.LowStockBookDTO;
import az.azal.libraff_book_store.util.PositionConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookStockService {

	private final BookStockRepository repository;

	private final EmployeeRepository employeeRepository;

	private final TransactionHistoryRepository historyRepository;

	private final StoreRepository storeRepository;

	private final BookRepository bookRepository;

	private final EmailService emailService;

	@Transactional
	public void restock(Map<Integer, Integer> restockedBooks, Integer storeId, Integer employeeId) {

		if (restockedBooks == null || restockedBooks.isEmpty()) {
			throw new MyException("Map is empty", "NOT_FOUND", HttpStatus.NOT_FOUND);
		}

		EmployeeEntity employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND));

		StoreEntity store = storeRepository.findById(storeId)
				.orElseThrow(() -> new MyException(ErrorStatus.STORE_NOT_FOUND));

		if (!employee.getIsActive()) {
			throw new MyException(ErrorStatus.UNAUTHORIZED_OPERATION);
		}

		for (Map.Entry<Integer, Integer> restockedBook : restockedBooks.entrySet()) {
			Integer bookId = restockedBook.getKey();
			Integer quantityToRestock = restockedBook.getValue();

			BookStockEntity stock = new BookStockEntity();

			Optional<BookStockEntity> optional = repository.findByBookIdAndStoreId(bookId, storeId);

			if (optional.isPresent()) {
				stock = optional.get();
				stock.setQuantity(stock.getQuantity() + quantityToRestock);
			} else {

				BookEntity book = bookRepository.findById(bookId).orElseThrow(
						() -> new MyException("Book not found in the master catalog!", ErrorStatus.BOOK_NOT_FOUND));

				stock = new BookStockEntity();
				stock.setBook(book);
				stock.setStore(store);
				stock.setQuantity(quantityToRestock);

			}

			repository.save(stock);

			TransactionHistoryEntity history = new TransactionHistoryEntity();
			history.setBook(stock.getBook());
			history.setQuantity(quantityToRestock);
			history.setSalesPrice(stock.getBook().getSalesPrice());
			history.setPurchasePrice(stock.getBook().getPurchasePrice());
			history.setTransactionType(TransactionType.RESTOCK);

			history.setStore(stock.getStore());
			history.setTransactionDate(LocalDateTime.now());
			history.setEmployee(employee);

			historyRepository.save(history);

		}

	}

	// @Scheduled(cron = "0 0 8 * * *")
	@Scheduled(cron = "*/25 * * * * *")
	@Transactional
	public void checkLowStockBooks() {

		List<BookStockEntity> books = repository.findByQuantityLessThanEqual(5);

		Map<String, List<LowStockBookDTO>> map = new HashMap<String, List<LowStockBookDTO>>();

		for (BookStockEntity book : books) {

			Optional<EmployeeEntity> manager = employeeRepository.findByStoreIdAndPositionIdAndIsActive(
					book.getStore().getId(), PositionConstants.STORE_MANAGER, true);

			LowStockBookDTO l = new LowStockBookDTO(book.getBook().getId(), book.getBook().getName(),
					book.getQuantity(), book.getStore().getName());

			if (manager.isEmpty())
				continue;

			map.computeIfAbsent(manager.get().getEmail(), k -> new ArrayList<>()).add(l);
		}

		emailService.sendLowStockNotification(map);

	}

}
