package az.azal.libraff_book_store.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.AuthorEntity;
import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.entity.GenreEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.AuthorRepository;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.repository.GenreRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.BookAddRequest;
import az.azal.libraff_book_store.response.BookAddResponse;
import az.azal.libraff_book_store.response.BookListResponse;
import az.azal.libraff_book_store.response.BookSingleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

	private final BookRepository repository;

	private final StoreRepository storeRepository;

	private final GenreRepository genreRepository;

	private final AuthorRepository authorRepository;

	private final ModelMapper mapper;

	public BookListResponse getAllBooks(Pageable p) {

		log.info("Fetching books from the database");

		Page<BookEntity> books = repository.findAll(p);

		log.debug("Books fetched: {} of {} total", books.getNumberOfElements(), books.getTotalElements());

		List<BookSingleResponse> responseList = new ArrayList<BookSingleResponse>();

		for (BookEntity book : books) {
			BookSingleResponse response = new BookSingleResponse();
			mapper.map(book, response);
			responseList.add(response);
		}

		BookListResponse listResponse = new BookListResponse();
		listResponse.setBooks(responseList);
		listResponse.setCurrentPage(books.getNumber());
		listResponse.setTotalPages(books.getTotalPages());
		listResponse.setTotalElements(books.getTotalElements());
		listResponse.setPageSize(books.getSize());
		log.info("Successfully fetched the books from database");

		return listResponse;
	}

	public BookAddResponse addBook(BookAddRequest request) {

		log.info("Adding a new Book: {}", request.getName());

		BookEntity book = new BookEntity();
		mapper.map(request, book);

		StoreEntity store = storeRepository.findById(request.getStoreId()).orElseThrow(() -> {
			log.warn("Store not found with id: {}", request.getStoreId());
			return new MyException(ErrorStatus.STORE_NOT_FOUND);
		});

		GenreEntity genre = genreRepository.findById(request.getGenreId()).orElseThrow(() -> {
			log.warn("Genre not found with id: {}", request.getGenreId());
			return new MyException(ErrorStatus.GENRE_NOT_FOUND);
		}

		);

		List<AuthorEntity> authors = authorRepository.findAllById(request.getAuthorIds());

		if (authors.size() != request.getAuthorIds().size()) {
			log.warn("One or more authors not found! Expected author size: {}. Actual auhtor size: {}", authors.size(),
					request.getAuthorIds().size());
			throw new MyException(ErrorStatus.AUTHOR_NOT_FOUND);
		}

		book.setStore(store);
		book.setGenre(genre);
		book.setAuthors(authors);

		repository.save(book);

		BookAddResponse response = new BookAddResponse();
		response.setId(book.getId());
		response.setName(book.getName());

		log.debug("Book saved with id: {}", book.getId());

		return response;
	}

}
