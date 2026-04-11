package az.azal.libraff_book_store.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookTransferAddRequest {

	@NotNull(message = "Book ID is required")
	private Integer bookId;

	@NotNull(message = "Employee ID is required")
	private Integer requestedByEmployeeId;

	@NotNull(message = "Source store ID is required")
	private Integer fromStoreId;

	@NotNull(message = "Destination store ID is required")
	private Integer toStoreId;

	@NotNull(message = "Quantity is required")
	@Min(value = 1, message = "Transfer quantity must be at least 1")
	private Integer quantity;

}
