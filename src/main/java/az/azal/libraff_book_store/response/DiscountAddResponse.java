package az.azal.libraff_book_store.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountAddResponse {

	private Integer id;

	private String discountName;

	private LocalDate discountStartDate;

	private LocalDate discountEndDate;

}
