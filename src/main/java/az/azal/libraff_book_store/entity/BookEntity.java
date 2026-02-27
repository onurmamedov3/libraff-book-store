package az.azal.libraff_book_store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "books")
public class BookEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String genre;

	private LocalDateTime datePublished;

	private BigDecimal purchasePrice;

	private BigDecimal salesPrice;

	private LocalDateTime purchaseDate;

	private LocalDateTime salesDate;

	private Integer publicationAmount;

	@ManyToMany(fetch = FetchType.LAZY) // Hibernate verilənlər bazasından məlumatı dərhal çəkmir;
										// yalnız müraciət edildikdə (proxy vasitəsilə) SQL sorğusu göndərir.
	@JoinTable( // foreign key üçün
			name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
	private Set<AuthorEntity> authors = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private StoreEntity store;

}
