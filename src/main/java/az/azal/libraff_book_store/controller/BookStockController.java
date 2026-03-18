package az.azal.libraff_book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.request.TransactionRestockRequest;
import az.azal.libraff_book_store.service.BookStockService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/stock")
@RequiredArgsConstructor
public class BookStockController {

	private final BookStockService service;

	@PostMapping(path = "/restock")
	public ResponseEntity<String> restockBook(@RequestBody TransactionRestockRequest request) {

		service.restock(request.getRestockedBooks(), request.getStoreId(), request.getEmployeeId());

		return new ResponseEntity<String>("Sucessfully restocked books!", HttpStatus.OK);

	}

}
