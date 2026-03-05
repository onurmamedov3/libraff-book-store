package az.azal.libraff_book_store.request;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

	@NotNull
	@Positive
	private Integer id;

	@Size(min = 7, max = 7, message = "FIN code must be exactly 7 characters")
	@Column(name = "fin", nullable = false, unique = true, updatable = false)
	private String FIN;

	private String name;

	private String surname;

	@Size(min = 6, message = "Password must be at least 6 characters long")
	private String password;

	private Boolean isActive;

	@Positive(message = "Position ID must be a positive number")
	private Integer positionId;

	@Email(message = "Must be a valid email format")
	private String email;

	private String phone;

	@Positive(message = "Salary must be greater than zero")
	private Double salary;

	@PastOrPresent(message = "Employment date cannot be in the future")
	private LocalDate dateEmployed;

	private LocalDate dateUnemployed;

	private Integer storeId;

}
