package az.azal.libraff_book_store.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.entity.BookStockEntity;
import az.azal.libraff_book_store.entity.BookTransferEntity;
import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.repository.BookStockRepository;
import az.azal.libraff_book_store.repository.BookTransferRepository;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.BookTransferAddRequest;
import az.azal.libraff_book_store.request.BookTransferApproveRequest;
import az.azal.libraff_book_store.util.PositionConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookTransferService {

	private final BookTransferRepository transferRepository;
	private final BookStockRepository stockRepository;
	private final EmployeeRepository employeeRepository;
	private final BookRepository bookRepository;
	private final StoreRepository storeRepository;

	@Transactional
	public void requestTransfer(BookTransferAddRequest request) {
		// 1. Validate employee exists and is active
		EmployeeEntity employee = employeeRepository.findById(request.getRequestedByEmployeeId())
				.orElseThrow(() -> new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND));

		if (!employee.getIsActive()) {
			throw new MyException("Inactive employees cannot request transfers", ErrorStatus.UNAUTHORIZED_OPERATION);
		}

		// 2. Validate Book and Stores
		BookEntity book = bookRepository.findById(request.getBookId())
				.orElseThrow(() -> new MyException(ErrorStatus.BOOK_NOT_FOUND));
		StoreEntity fromStore = storeRepository.findById(request.getFromStoreId())
				.orElseThrow(() -> new MyException("Source store not found", ErrorStatus.STORE_NOT_FOUND));
		StoreEntity toStore = storeRepository.findById(request.getToStoreId())
				.orElseThrow(() -> new MyException("Destination store not found", ErrorStatus.STORE_NOT_FOUND));

		if (fromStore.getId().equals(toStore.getId())) {
			throw new MyException("You cannot transfer from the same store!", ErrorStatus.INVALID_OPERATION);
		}

		// 3. Verify that the source store actually has enough stock before allowing the
		// request
		BookStockEntity sourceStock = stockRepository.findByBookIdAndStoreId(book.getId(), fromStore.getId())
				.orElseThrow(() -> new MyException("Book not available in the source store inventory",
						ErrorStatus.STOCK_NOT_FOUND));

		if (sourceStock.getQuantity() < request.getQuantity()) {
			throw new MyException("Source store does not have enough stock", ErrorStatus.INSUFFICIENT_STOCK);
		}

		// 4. Create and save the PENDING transfer request
		BookTransferEntity transfer = new BookTransferEntity();
		transfer.setBook(book);
		transfer.setRequestedBy(employee);
		transfer.setFromStore(fromStore);
		transfer.setToStore(toStore);
		transfer.setQuantity(request.getQuantity());
		transfer.setStatus("PENDING");
		transfer.setIsApproved(false);

		transferRepository.save(transfer);
	}

	@Transactional
	public void processTransferApproval(Integer transferId, BookTransferApproveRequest request) {
		// 1. Fetch Transfer Request
		BookTransferEntity transfer = transferRepository.findById(transferId).orElseThrow(
				() -> new MyException("Transfer request not found", "TRANSFER_NOT_FOUND", HttpStatus.NOT_FOUND));

		if (!transfer.getStatus().equals("PENDING")) {
			throw new MyException("Only pending transfers can be approved or rejected", ErrorStatus.INVALID_STATUS);
		}

		// 2. Validate Manager
		EmployeeEntity manager = employeeRepository.findById(request.getManagerId())
				.orElseThrow(() -> new MyException(ErrorStatus.MANAGER_NOT_FOUND));

		if (!manager.getIsActive() || manager.getPosition().getId() != PositionConstants.STORE_MANAGER) {
			throw new MyException("Only active store managers can approve transfers", "UNAUTHORIZED",
					HttpStatus.FORBIDDEN);
		}

		// 3. Process the Decision
		if (request.getStatus().equalsIgnoreCase("APPROVED")) {

			// Re-check stock in case it was sold while the request was pending
			BookStockEntity sourceStock = stockRepository
					.findByBookIdAndStoreId(transfer.getBook().getId(), transfer.getFromStore().getId())
					.orElseThrow(() -> new MyException("Source stock missing", ErrorStatus.STOCK_NOT_FOUND));

			if (sourceStock.getQuantity() < transfer.getQuantity()) {
				throw new MyException("Source store no longer has enough stock to fulfill this approved request",
						ErrorStatus.INSUFFICIENT_STOCK);
			}

			// Deduct from Source Store
			sourceStock.setQuantity(sourceStock.getQuantity() - transfer.getQuantity());
			stockRepository.save(sourceStock);

			// Add to Destination Store (Create stock record if it doesn't exist yet)
			BookStockEntity destStock = stockRepository
					.findByBookIdAndStoreId(transfer.getBook().getId(), transfer.getToStore().getId()).orElseGet(() -> {
						BookStockEntity newStock = new BookStockEntity();
						newStock.setBook(transfer.getBook());
						newStock.setStore(transfer.getToStore());
						newStock.setQuantity(0);
						return newStock;
					});

			destStock.setQuantity(destStock.getQuantity() + transfer.getQuantity());
			stockRepository.save(destStock);

			// Update transfer record
			transfer.setStatus("APPROVED");
			transfer.setIsApproved(true);

		} else if (request.getStatus().equalsIgnoreCase("REJECTED")) {
			transfer.setStatus("REJECTED");
			transfer.setIsApproved(false);
		} else {
			throw new MyException("Status must be APPROVED or REJECTED", ErrorStatus.INVALID_STATUS);
		}

		transferRepository.save(transfer);
	}

}
