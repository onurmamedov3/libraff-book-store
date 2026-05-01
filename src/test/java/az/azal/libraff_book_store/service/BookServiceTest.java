package az.azal.libraff_book_store.service;

// 1. Static imports make testing syntax much cleaner to read
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

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

// 2. This tells Spring we are using Mockito to create fake dependencies
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	// 3. @Mock creates "fake" versions of all your dependencies
	@Mock
	private BookRepository repository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private GenreRepository genreRepository;
	@Mock
	private AuthorRepository authorRepository;
	@Mock
	private Pageable pageable;
	@Mock
	private ModelMapper mapper;

	// 4. @InjectMocks creates the REAL service, but injects the FAKES above into it
	@InjectMocks
	private BookService bookService;

	@Test
	void addBook_Success_WhenAllDataIsValid() {
		// --- STEP A: GIVEN (Arrange our fake data) ---

		BookAddRequest request = new BookAddRequest();
		request.setName("Clean Code");
		request.setStoreId(1);
		request.setGenreId(2);
		request.setAuthorIds(List.of(3));

		StoreEntity fakeStore = new StoreEntity();
		fakeStore.setId(1);

		GenreEntity fakeGenre = new GenreEntity();
		fakeGenre.setId(2);

		AuthorEntity fakeAuthor = new AuthorEntity();
		fakeAuthor.setId(3);

		// Teach our fakes how to respond when the service calls them
		when(storeRepository.findById(1)).thenReturn(Optional.of(fakeStore));
		when(genreRepository.findById(2)).thenReturn(Optional.of(fakeGenre));
		when(authorRepository.findAllById(request.getAuthorIds())).thenReturn(List.of(fakeAuthor));

		// Teach the fake ModelMapper to copy the name over
		doAnswer(invocation -> {
			BookEntity entity = invocation.getArgument(1);
			entity.setName(request.getName());
			return null; // mapper.map() returns void
		}).when(mapper).map(eq(request), any(BookEntity.class));

		// Simulate the database saving the book and assigning it ID #100
		doAnswer(invocation -> {
			BookEntity savedBook = invocation.getArgument(0);
			savedBook.setId(100);
			return savedBook;
		}).when(repository).save(any(BookEntity.class));

		// --- STEP B: WHEN (Execute the method we are testing) ---

		BookAddResponse response = bookService.addBook(request);

		// --- STEP C: THEN (Assert the results match our expectations) ---

		assertNotNull(response);
		assertEquals(100, response.getId());
		assertEquals("Clean Code", response.getName());

		// Verify that repository.save() was called exactly 1 time
		verify(repository, times(1)).save(any(BookEntity.class));
	}

	@Test
	void addBook_ThrowsException_WhenStoreIsNotFound() {
		// --- STEP A: GIVEN ---
		BookAddRequest request = new BookAddRequest();
		request.setStoreId(99); // A store ID that doesn't exist

		// Teach the fake repository to return empty (simulating store not found)
		when(storeRepository.findById(99)).thenReturn(Optional.empty());

		// --- STEP B & C: WHEN & THEN ---
		// Assert that calling this method throws your custom MyException
		MyException exception = assertThrows(MyException.class, () -> {
			bookService.addBook(request);
		});

		// Verify the exception has the exact data you programmed into it
		assertEquals(ErrorStatus.STORE_NOT_FOUND.getMessage(), exception.getMessage());
		assertEquals(ErrorStatus.STORE_NOT_FOUND.getErrorCode(), exception.getErrorCode());
		assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

		// Verify the database save method was NEVER called because the code stopped
		// early
		verify(repository, never()).save(any());
	}

	@Test
	void addBook_ThrowsException_WhenGenreIsNotFound() {
		// Given
		BookAddRequest request = new BookAddRequest();
		request.setGenreId(77);

		// Set fake store for checking genre
		StoreEntity store = new StoreEntity();
		store.setId(1);
		request.setStoreId(1);

		// When/Then
		when(storeRepository.findById(1)).thenReturn(Optional.of(store));
		when(genreRepository.findById(77)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> bookService.addBook(request));

		// Verify
		assertEquals(ErrorStatus.GENRE_NOT_FOUND.getMessage(), exception.getMessage());
		assertEquals(ErrorStatus.GENRE_NOT_FOUND.getErrorCode(), exception.getErrorCode());
		assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

		verify(repository, never()).save(any());

	}

	@Test
	void addBook_ThrowsException_WhenAuthorIsNotFound() {
		// Given
		BookAddRequest request = new BookAddRequest();
		request.setStoreId(1);
		request.setGenreId(1);
		request.setAuthorIds(List.of(1));

		StoreEntity store = new StoreEntity();
		store.setId(1);

		GenreEntity genre = new GenreEntity();
		genre.setId(1);

		// When/Then
		when(storeRepository.findById(1)).thenReturn(Optional.of(store));
		when(genreRepository.findById(1)).thenReturn(Optional.of(genre));
		when(authorRepository.findAllById(request.getAuthorIds())).thenReturn(List.of());

		MyException exception = assertThrows(MyException.class, () -> bookService.addBook(request));

		assertEquals(ErrorStatus.AUTHOR_NOT_FOUND.getMessage(), exception.getMessage());
		assertEquals(ErrorStatus.AUTHOR_NOT_FOUND.getErrorCode(), exception.getErrorCode());
		assertEquals(ErrorStatus.AUTHOR_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());

		verify(repository, never()).save(any());
	}

	@Test
	void getAllBooks_ReturnBooks_WhenBooksExist() {

		BookEntity book1 = new BookEntity();
		book1.setId(1);
		book1.setName("Clean Code");

		BookEntity book2 = new BookEntity();
		book2.setId(2);
		book2.setName("Fundamentals of Programming");

		when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(book1, book2)));

		doAnswer(invocation -> {
			BookEntity source = invocation.getArgument(0);
			BookSingleResponse target = invocation.getArgument(1);
			target.setName(source.getName());
			return null;
		}).when(mapper).map(any(BookEntity.class), any(BookSingleResponse.class));

		BookListResponse result = bookService.getAllBooks(PageRequest.of(0, 10));

		assertNotNull(result);
		assertEquals(2, result.getBooks().size());
		assertEquals("Clean Code", result.getBooks().get(0).getName());
		assertEquals("Fundamentals of Programming", result.getBooks().get(1).getName());

		assertEquals(0, result.getCurrentPage());
		assertEquals(1, result.getTotalPages());
		assertEquals(2, result.getTotalElements());

		verify(repository, times(1)).findAll(pageable);
	}

	@Test
	void getAllBooks_ReturnsEmptyList_WhenNoBooksExists() {

		// Given
		when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

		// When
		BookListResponse result = bookService.getAllBooks(PageRequest.of(0, 10));

		// Then
		assertNotNull(result);
		assertEquals(0, result.getBooks().size());
		verify(repository, times(1)).findAll(pageable);
		verify(mapper, never()).map(any(), any());
	}
}