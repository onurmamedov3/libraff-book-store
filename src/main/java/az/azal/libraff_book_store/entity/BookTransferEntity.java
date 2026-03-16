package az.azal.libraff_book_store.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "book_transfers")

public class BookTransferEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private BookEntity book;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private EmployeeEntity requestedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_store_id", nullable = false)
	private StoreEntity fromStore; // Source (Branch B)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_store_id", nullable = false)
	private StoreEntity toStore; // Destination (Branch A)

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "transfer_date")
	private LocalDateTime transferDate = LocalDateTime.now();

	@Column(name = "is_approved")
	private Boolean isApproved = false;

	private String status = "PENDING"; // PENDING, APPROVED, REJECTED
}