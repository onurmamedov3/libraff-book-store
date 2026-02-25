package az.azal.libraff_book_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class EmployeeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String FIN;

	private String name;

	private String surname;

	private String password;

	private Boolean isActive;

	private Integer positionId; // 1 - Kassir, 2 - Müdir, ...

	private String email;

	private String phone;

	private Double salary;

	private LocalDate dateEmployed;

	private LocalDate dateUnemployed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private StoreEntity store;
}