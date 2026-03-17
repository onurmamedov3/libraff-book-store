package az.azal.libraff_book_store.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAddRequest {

	@NotBlank(message = "Book name cannot be blank")
	private String name;

	@NotNull(message = "Publication date is required")
	@PastOrPresent(message = "Publication date cannot be in the future")
	private LocalDateTime datePublished;

	@NotNull(message = "Purchase price is required")
	@Positive(message = "Purchase price must be greater than zero")
	private Double purchasePrice;

	@NotNull(message = "Sales price is required")
	@Positive(message = "Sales price must be greater than zero")
	private Double salesPrice;

	@NotNull(message = "Publication amount is required")
	@Min(value = 1, message = "Publication amount must be at least 1")
	private Integer publicationAmount;

	// We take a list of Author IDs from the frontend, not the full objects
	@NotEmpty(message = "At least one author ID must be provided")
	private List<Integer> authorIds;

	// We take the Store ID to link the book to a specific branch
	@NotNull(message = "Store ID is required")
	@Positive(message = "Store ID must be a positive number")
	private Integer storeId;

	// We take the Genre ID to categorize the book
	@NotNull(message = "Genre ID is required")
	@Positive(message = "Genre ID must be a positive number")
	private Integer genreId;

}