package az.azal.libraff_book_store.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_transfer")
public class EmployeeTransferEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String surname;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private EmployeeEntity employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_store_id")
	private StoreEntity fromStore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_store_id")
	private StoreEntity toStore;

	private LocalDateTime transferDate;

}
