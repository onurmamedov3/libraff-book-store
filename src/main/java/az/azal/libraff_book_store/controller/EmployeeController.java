package az.azal.libraff_book_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.request.EmployeeAddRequest;
import az.azal.libraff_book_store.response.EmployeeAddResponse;
import az.azal.libraff_book_store.response.EmployeeListResponse;
import az.azal.libraff_book_store.service.EmployeeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/employees")
public class EmployeeController {

	@Autowired
	private EmployeeService service;

	@PostMapping
	public ResponseEntity<EmployeeAddResponse> addEmployee(@Valid @RequestBody EmployeeAddRequest request,
			BindingResult br) {

		if (br.hasErrors()) {
			throw new MyException("Validation failed", br, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
		}
		EmployeeAddResponse response = service.add(request);
		return new ResponseEntity<EmployeeAddResponse>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public EmployeeListResponse getAllEmployees() {
		return service.getAll();
	}

}
