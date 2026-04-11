package az.azal.libraff_book_store.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "discounts")
public class DiscountEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "discount_name", nullable = false)
	private String discountName;

	@Column(name = "discount_percentage", nullable = false)
	@Min(value = 5, message = "Endirim minimum 5% olmalıdır")
	@Max(value = 40, message = "Endirim maksimum 40% ola bilər")
	private Double discountPercentage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id")
	private BookEntity book;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private AuthorEntity author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "genre_id")
	private GenreEntity genre;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private StoreEntity store;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "discount_start_date", nullable = false)
	private LocalDate discountStartDate;

	@Column(name = "discount_end_date", nullable = false)
	private LocalDate discountEndDate;
}