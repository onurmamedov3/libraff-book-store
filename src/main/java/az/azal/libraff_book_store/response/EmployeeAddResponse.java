package az.azal.libraff_book_store.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAddResponse {
	
	private Integer id;
	
	private String name;
	
	private Boolean isActive;
	
	private String email;
	
	private LocalDate dateEmployed;
	
	private String storeName;
	
	// storeName and role may be added later...
	
}
