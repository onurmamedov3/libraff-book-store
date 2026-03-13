package az.azal.libraff_book_store.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountAddRequest {

	@NotBlank(message = "Endirimin adı boş ola bilməz")
	private String discountName;

	@NotNull(message = "Endirim faizi qeyd edilməlidir")
	@Min(value = 5, message = "Endirim minimum 5% ola bilər")
	@Max(value = 40, message = "Endirim maksimum 40% ola bilər")
	private Double discountPercentage;

	private Integer bookId;

	private Integer authorId;

	private Integer genreId;

	private Integer storeId;

	@NotNull(message = "Başlama tarixi mütləqdir")
	private LocalDate discountStartDate;

	@NotNull(message = "Bitmə tarixi mütləqdir")
	private LocalDate discountEndDate;

}
