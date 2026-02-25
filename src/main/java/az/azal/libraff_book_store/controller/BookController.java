package az.azal.libraff_book_store.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.response.BookListResponse;
import az.azal.libraff_book_store.response.BookSingleResponse;
import az.azal.libraff_book_store.service.BookService;

@RestController
@RequestMapping(path = "/books")
public class BookController {

	@Autowired
	private BookService service;

	@GetMapping
	public BookListResponse getAll() {
		return service.getAllBooks();
	}

}
