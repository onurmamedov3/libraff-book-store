package az.azal.libraff_book_store.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
	@PreAuthorize("hasAuthority('ROLE_RESTOCK_BOOK')")
	@RateLimiter(name = "restockBook",fallbackMethod = "restockBookFallback")
	public ResponseEntity<String> restockBook(@RequestBody TransactionRestockRequest request) {

		service.restock(request.getRestockedBooks(), request.getStoreId(), request.getEmployeeId());

		return new ResponseEntity<String>("Sucessfully restocked books!", HttpStatus.OK);

	}

	private ResponseEntity<String> restockBookFallback(TransactionRestockRequest request, Throwable t) {
		return new ResponseEntity<String>("Too many requests! Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
	}
}
