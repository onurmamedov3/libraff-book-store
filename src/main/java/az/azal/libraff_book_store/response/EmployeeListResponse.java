package az.azal.libraff_book_store.response;

import java.util.List;

import lombok.Data;

@Data
public class EmployeeListResponse {

	List<EmployeeSingleResponse> employees;

}
