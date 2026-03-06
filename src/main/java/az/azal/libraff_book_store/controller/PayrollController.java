package az.azal.libraff_book_store.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.service.PayrollService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/payroll")
@RequiredArgsConstructor
public class PayrollController {

	private final PayrollService service;

//	@PostMapping
//	public ResponseEntity<?> payMonthlySalary() {
//
//		return ResponseEntity.ok().build();
//	}

}
