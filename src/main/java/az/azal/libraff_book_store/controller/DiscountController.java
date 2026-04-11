package az.azal.libraff_book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.enums.ErrorStatus;
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
	@PreAuthorize("hasAuthority('ROLE_ADD_DISCOUNT')")
	public ResponseEntity<DiscountAddResponse> createDiscount(@Valid @RequestBody DiscountAddRequest request,
			BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException(ErrorStatus.VALIDATION_ERROR, br);
		}
		DiscountAddResponse response = service.createDiscount(request);
		return new ResponseEntity<DiscountAddResponse>(response, HttpStatus.CREATED);
	}

}
