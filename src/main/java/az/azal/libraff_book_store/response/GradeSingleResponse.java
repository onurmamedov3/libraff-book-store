package az.azal.libraff_book_store.response;

import az.azal.libraff_book_store.enums.GradeFrequency;
import az.azal.libraff_book_store.enums.GradeTarget;
import az.azal.libraff_book_store.enums.GradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeSingleResponse {

	private Integer id;

	private String bonusName;

	private Double bonusAmount;

	private Double bonusPercentage;

	private Double minSalesThreshold;

	private GradeType bonusType;

	private GradeTarget targetType;

	private GradeFrequency bonusFrequency;

}