package az.azal.libraff_book_store.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAddRequest {
    
    @NotBlank(message = "FIN code is mandatory")
    @Size(min = 7, max = 7, message = "FIN code must be exactly 7 characters")
    @Column(unique = true)
    private String FIN; 

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    private String surname;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "Active status must be provided")
    private Boolean isActive;

    @NotNull(message = "Position ID is required")
    @Positive(message = "Position ID must be a positive number")
    private Integer positionId; 

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Must be a valid email format")
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    private String phone;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be greater than zero")
    private Double salary;

    @NotNull(message = "Employment date is required")
    @PastOrPresent(message = "Employment date cannot be in the future")
    private LocalDate dateEmployed;

    private LocalDate dateUnemployed;

    // REAL-WORLD SCENARIO: Just take the ID from the frontend!
    @NotNull(message = "Store ID is required to assign the employee")
    private Integer storeId;
}