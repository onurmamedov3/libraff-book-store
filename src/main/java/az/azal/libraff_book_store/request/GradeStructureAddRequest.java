package az.azal.libraff_book_store.request;

import java.util.List;

import az.azal.libraff_book_store.enums.GradeFrequency;
import az.azal.libraff_book_store.enums.GradeTarget;
import az.azal.libraff_book_store.enums.GradeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeStructureAddRequest {

	@NotBlank(message = "Bonus name cannot be blank")
	private String bonusName;

	@Min(value = 0, message = "Bonus amount cannot be negative")
	private Double bonusAmount;

	@Min(value = 0, message = "Bonus percentage cannot be negative")
	private Double bonusPercentage;

	@NotNull(message = "Minimum sales threshold is required")
	@Min(value = 0, message = "Sales threshold cannot be negative")
	private Double minSalesThreshold;

	@NotNull(message = "Bonus type is required")
	private GradeType bonusType;

	@NotNull(message = "Target type is required")
	private GradeTarget targetType;

	@NotNull(message = "Bonus frequency is required")
	private GradeFrequency bonusFrequency;

	// Optional: Only used if targetType is STORE
	private List<Integer> assignedStoreIds;

	// Optional: Only used if targetType is EMPLOYEE
	private List<Integer> assignedPositionIds;

}