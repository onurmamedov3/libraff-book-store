package az.azal.libraff_book_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.request.BookAddRequest;
import az.azal.libraff_book_store.response.BookAddResponse;
import az.azal.libraff_book_store.response.BookListResponse;
import az.azal.libraff_book_store.service.BookService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/books")
public class BookController {

	@Autowired
	private BookService service;

	@GetMapping
	public BookListResponse getAll(@PageableDefault(size = 10, page = 0) Pageable pageable) {
		return service.getAllBooks(pageable);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_ADD_BOOK')")
	public ResponseEntity<BookAddResponse> addBook(@Valid @RequestBody BookAddRequest request, BindingResult br) {
		if (br.hasErrors()) {
			throw new MyException(ErrorStatus.VALIDATION_ERROR, br);
		}
		BookAddResponse response = service.addBook(request);
		return new ResponseEntity<BookAddResponse>(response, HttpStatus.CREATED);
	}

}
