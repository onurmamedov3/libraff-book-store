package az.azal.libraff_book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.request.BookTransferAddRequest;
import az.azal.libraff_book_store.request.BookTransferApproveRequest;
import az.azal.libraff_book_store.service.BookTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/transfers")
@RequiredArgsConstructor
public class BookTransferController {

	private final BookTransferService service;

	@PostMapping("/request")
	public ResponseEntity<?> requestTransfer(@Valid @RequestBody BookTransferAddRequest request, BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException("Validation failed", br, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
		}
		service.requestTransfer(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("Transfer requested successfully.");
	}

	@PatchMapping("/{transferId}/approve")
	public ResponseEntity<?> approveTransfer(@PathVariable Integer transferId,
			@Valid @RequestBody BookTransferApproveRequest request, BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException("Validation failed", br, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
		}
		service.processTransferApproval(transferId, request);
		return ResponseEntity.ok("Transfer status updated to: " + request.getStatus());
	}

}
