package az.azal.libraff_book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.request.DiscountAddRequest;
import az.azal.libraff_book_store.response.DiscountAddResponse;
import az.azal.libraff_book_store.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/discounts")
@RequiredArgsConstructor
public class DiscountController {

	private final DiscountService service;

	@PostMapping
	public ResponseEntity<DiscountAddResponse> createDiscount(@Valid @RequestBody DiscountAddRequest request,
			BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException("Validation failed", br, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
		}
		DiscountAddResponse response = service.createDiscount(request);
		return new ResponseEntity<DiscountAddResponse>(response, HttpStatus.CREATED);
	}

}
