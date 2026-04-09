package az.azal.libraff_book_store.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.BookStockEntity;
import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.enums.TransactionType;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.BookStockRepository;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

	private final BookStockRepository bookStockRepository;

	private final TransactionHistoryRepository historyRepository;

	private final EmployeeRepository employeeRepository;

	private final DiscountService discountService;

	@Transactional
	public void sellBook(Map<Integer, Integer> soldBooks, Integer storeId, Integer employeeId) {

		EmployeeEntity employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND));

		if (employee.getIsActive() != true) {
			throw new MyException(ErrorStatus.UNAUTHORIZED_OPERATION);
		}

		for (Map.Entry<Integer, Integer> entry : soldBooks.entrySet()) {
			Integer bookId = entry.getKey();
			Integer quantityToSell = entry.getValue();

			// 1. Find Stock
			BookStockEntity stock = bookStockRepository.findByBookIdAndStoreId(bookId, storeId)
					.orElseThrow(() -> new MyException(ErrorStatus.BOOK_NOT_FOUND));

			// 2. Check Stock
			if (stock.getQuantity() < quantityToSell) {
				throw new MyException("Not enough stock for: " + stock.getBook().getName(),
						ErrorStatus.NOT_ENOUGH_STOCK);
			}

			Double finalDiscountedPrice = discountService.calculateDiscountedPrice(stock.getBook(), storeId);

			// 3. Deduct Stock
			stock.setQuantity(stock.getQuantity() - quantityToSell);
			bookStockRepository.save(stock);

			// 4. SAVE THE HISTORY
			TransactionHistoryEntity history = new TransactionHistoryEntity();
			history.setBook(stock.getBook());
			history.setQuantity(quantityToSell);
			history.setSalesPrice(finalDiscountedPrice);
			history.setPurchasePrice(stock.getBook().getPurchasePrice());
			history.setTransactionType(TransactionType.SALE);

			history.setStore(stock.getStore());
			history.setTransactionDate(LocalDate.now());
			history.setEmployee(employee);

			historyRepository.save(history);
		}
	}
}
