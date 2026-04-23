package az.azal.libraff_book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.request.TransactionSaleRequest;
import az.azal.libraff_book_store.response.BillResponse;
import az.azal.libraff_book_store.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/transaction-history")
@RequiredArgsConstructor
public class TransactionHistoryController {

	private final TransactionHistoryService service;

	@PostMapping(path = "/sell")
	@PreAuthorize("hasAuthority('ROLE_SELL_BOOK')")
	public ResponseEntity<BillResponse> sellBook(@RequestBody TransactionSaleRequest request) {

		BillResponse response = service.sellBook(request.getSoldBooks(), request.getStoreId(), request.getEmployeeId());

		return new ResponseEntity<BillResponse>(response, HttpStatus.CREATED);
	}

}
