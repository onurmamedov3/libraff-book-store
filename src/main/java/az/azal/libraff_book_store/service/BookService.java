package az.azal.libraff_book_store.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.AuthorEntity;
import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.entity.GenreEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.AuthorRepository;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.repository.GenreRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.BookAddRequest;
import az.azal.libraff_book_store.response.BookAddResponse;
import az.azal.libraff_book_store.response.BookListResponse;
import az.azal.libraff_book_store.response.BookSingleResponse;

@Service
public class BookService {

	@Autowired
	private BookRepository repository; // @RequiredArgsConstructor

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private GenreRepository genreRepository;

	@Autowired
	private AuthorRepository authorRepository;

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

	public BookAddResponse addBook(BookAddRequest request) {

		BookEntity book = new BookEntity();
		mapper.map(request, book);

		StoreEntity store = storeRepository.findById(request.getStoreId())
				.orElseThrow(() -> new MyException("Book not found!", "BOOK_NOT_FOUND", HttpStatus.NOT_FOUND));

		GenreEntity genre = genreRepository.findById(request.getGenreId())
				.orElseThrow(() -> new MyException("Genre not found!", "GENRE_NOT_FOUND", HttpStatus.NOT_FOUND));

		List<AuthorEntity> authors = authorRepository.findAllById(request.getAuthorIds());

		if (authors.size() != request.getAuthorIds().size()) {
			throw new MyException("One or more authors not found!", "AUTHOR_NOT_FOUND", HttpStatus.NOT_FOUND);
		}

		book.setStore(store);
		book.setGenre(genre);
		book.setAuthors(authors);

		repository.save(book);

		BookAddResponse response = new BookAddResponse();
		response.setId(book.getId());
		response.setName(book.getName());

		return response;
	}

}
