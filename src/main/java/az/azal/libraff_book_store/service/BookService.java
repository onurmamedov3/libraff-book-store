package az.azal.libraff_book_store.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.response.BookListResponse;
import az.azal.libraff_book_store.response.BookSingleResponse;

@Service
public class BookService {

	@Autowired
	private BookRepository repository; // @RequiredArgsConstructor

	@Autowired
	private ModelMapper mapper;

	public BookListResponse getAllBooks() {

		List<BookEntity> books = repository.findAll();
		List<BookSingleResponse> responseList = new ArrayList<BookSingleResponse>();

		for (BookEntity book : books) {
			BookSingleResponse response = new BookSingleResponse();
			mapper.map(book, response);
			responseList.add(response);
		}

		BookListResponse listResponse = new BookListResponse();
		listResponse.setBooks(responseList);
		return listResponse;
	}

}
