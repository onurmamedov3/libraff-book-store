package az.azal.libraff_book_store.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookTransferApproveRequest {

	@NotNull(message = "Manager ID is required")
	private Integer managerId;

	@NotBlank(message = "Status must be APPROVED or REJECTED")
	private String status; // "APPROVED" or "REJECTED"
}