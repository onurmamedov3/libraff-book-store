package az.azal.libraff_book_store.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.request.EmployeeAddRequest;
import az.azal.libraff_book_store.request.EmployeeUpdateRequest;
import az.azal.libraff_book_store.response.EmployeeAddResponse;
import az.azal.libraff_book_store.response.EmployeeListResponse;
import az.azal.libraff_book_store.response.EmployeeSingleResponse;
import az.azal.libraff_book_store.service.EmployeeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/employees")
@RateLimiter(name = "employeeController")
public class EmployeeController {

	@Autowired
	private EmployeeService service;

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_ADD_EMPLOYEE')")
	public ResponseEntity<EmployeeAddResponse> addEmployee(@Valid @RequestBody EmployeeAddRequest request,
			BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException(ErrorStatus.VALIDATION_ERROR, br);
		}
		EmployeeAddResponse response = service.add(request);
		return new ResponseEntity<EmployeeAddResponse>(response, HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_GET_EMPLOYEE')")
	public EmployeeListResponse getAllEmployees() {
		return service.getAll();
	}

	@GetMapping(path = "/{id}")
	@PreAuthorize("hasAuthority('ROLE_GET_EMPLOYEE')")
	public EmployeeSingleResponse findEmployeeById(@PathVariable Integer id) {
		return service.findEmployeeById(id);
	}

	@DeleteMapping(path = "/{id}")
	@PreAuthorize("hasAuthority('ROLE_DELETE_EMPLOYEE')")
	public ResponseEntity<?> deleteEmployeeById(@PathVariable Integer id) {
		service.deleteEmployeeById(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping
	@PreAuthorize("hasAuthority('ROLE_PATCH_EMPLOYEE')")
	public ResponseEntity<?> patchEmployee(@Valid @RequestBody EmployeeUpdateRequest request, BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException(ErrorStatus.VALIDATION_ERROR, br);
		}
		service.patchEmployee(request);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(path = "/{id}")
	@PreAuthorize("hasAuthority('ROLE_REHIRE_EMPLOYEE')")
	public ResponseEntity<?> rehireEmployee(@PathVariable Integer id) {

		service.rehireEmployeeById(id);

		return ResponseEntity.noContent().build();
	}

}
