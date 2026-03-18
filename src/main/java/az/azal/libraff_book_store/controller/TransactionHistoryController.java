package az.azal.libraff_book_store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.request.TransactionSaleRequest;
import az.azal.libraff_book_store.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/transaction-history")
@RequiredArgsConstructor
public class TransactionHistoryController {

	private final TransactionHistoryService service;

	@PostMapping(path = "/sell")
	public ResponseEntity<?> sellBook(@RequestBody TransactionSaleRequest request) {

		service.sellBook(request.getSoldBooks(), request.getStoreId(), request.getEmployeeId());

		return ResponseEntity.ok().build();
	}

}
