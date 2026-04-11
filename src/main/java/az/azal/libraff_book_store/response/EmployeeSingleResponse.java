package az.azal.libraff_book_store.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSingleResponse {

	private Integer id;

	private String name;

	private String surname;

	// private String position;

}
