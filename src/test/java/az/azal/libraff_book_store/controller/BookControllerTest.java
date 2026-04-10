package az.azal.libraff_book_store.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import az.azal.libraff_book_store.request.BookAddRequest;
import az.azal.libraff_book_store.response.BookAddResponse;
import az.azal.libraff_book_store.response.BookListResponse;
import az.azal.libraff_book_store.response.BookSingleResponse;
import az.azal.libraff_book_store.service.BookService;
import az.azal.libraff_book_store.service.UserDetailsServiceImpl;
import az.azal.libraff_book_store.util.JwtUtil;
import lombok.RequiredArgsConstructor;

// 1. Tells Spring to ONLY load the Web Layer and this specific controller
@WebMvcTest(controllers = BookController.class)
// 2. Disables security just for this test so we don't get 401/403 errors while testing validation
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class BookControllerTest {

	private final MockMvc mockMvc; // Our "fake web browser"

	private final ObjectMapper objectMapper; // Converts Java objects to JSON strings

	// 3. We Mock the Service because we don't care about business logic here
	@MockBean
	private BookService bookService;

	@MockBean
	private JwtUtil jwtUtil;

	@MockBean
	private UserDetailsServiceImpl userDetailsService;

	// =========================================================
	// GET /books
	// =========================================================

	@Test
	void getAll_Returns200_WithEmptyBookList() throws Exception {
		BookListResponse fakeResponse = new BookListResponse();
		fakeResponse.setBooks(List.of()); // empty list

		when(bookService.getAllBooks()).thenReturn(fakeResponse);

		mockMvc.perform(get("/books").contentType(MediaType.APPLICATION_JSON))

				.andExpect(status().isOk())

				.andExpect(jsonPath("$.books").isArray())

				.andExpect(jsonPath("$.books").isEmpty());
	}

	@Test
	void getAll_Returns200_WithPopulatedBookList() throws Exception {
		// Build a fake response with actual books inside
		BookSingleResponse book1 = new BookSingleResponse();
		book1.setName("Clean Code");

		BookSingleResponse book2 = new BookSingleResponse();
		book2.setName("Effective Java");

		BookListResponse fakeResponse = new BookListResponse();
		fakeResponse.setBooks(List.of(book1, book2));

		when(bookService.getAllBooks()).thenReturn(fakeResponse);

		mockMvc.perform(get("/books").contentType(MediaType.APPLICATION_JSON))

				.andExpect(status().isOk())

				.andExpect(jsonPath("$.books").isArray())

				.andExpect(jsonPath("$.books.length()").value(2))

				.andExpect(jsonPath("$.books[0].name").value("Clean Code"))

				.andExpect(jsonPath("$.books[1].name").value("Effective Java"));
	}

	@Test
	void addBook_Returns201_WhenRequestIsValid() throws Exception {
		BookAddRequest validRequest = buildValidRequest();

		BookAddResponse fakeResponse = new BookAddResponse();
		fakeResponse.setId(100);
		fakeResponse.setName("Fundamentals of Programming");

		when(bookService.addBook(ArgumentMatchers.any(BookAddRequest.class))).thenReturn(fakeResponse);

		mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))

				.andExpect(status().isCreated())

				.andExpect(jsonPath("$.id").value(100))

				.andExpect(jsonPath("$.name").value("Fundamentals of Programming"));
	}

	// =========================================================
	// POST /books — Validation failures (400)
	// =========================================================

	@Test
	void addBook_Returns400_WhenNameIsBlank() throws Exception {
		BookAddRequest request = buildValidRequest();
		request.setName(""); // violates @NotBlank

		performPostAndExpect400(request);
	}

	@Test
	void addBook_Returns400_WhenNameIsNull() throws Exception {
		BookAddRequest request = buildValidRequest();
		request.setName(null); // also violates @NotBlank

		performPostAndExpect400(request);
	}

	@Test
	void addBook_Returns400_WhenPurchasePriceIsNegative() throws Exception {
		BookAddRequest request = buildValidRequest();
		request.setPurchasePrice(-5.0); // violates @Positive

		performPostAndExpect400(request);
	}

	@Test
	void addBook_Returns400_WhenSalesPriceIsNegative() throws Exception {
		BookAddRequest request = buildValidRequest();
		request.setSalesPrice(-1.0); // violates @Positive

		performPostAndExpect400(request);
	}

	@Test
	void addBook_Returns400_WhenAuthorIdsIsEmpty() throws Exception {
		BookAddRequest request = buildValidRequest();
		request.setAuthorIds(List.of()); // violates @NotEmpty

		performPostAndExpect400(request);
	}

	@Test
	void addBook_Returns400_WhenPublicationAmountIsZero() throws Exception {
		BookAddRequest request = buildValidRequest();
		request.setPublicationAmount(0); // violates @Positive

		performPostAndExpect400(request);
	}

	// =========================================================
	// Shared helper methods
	// =========================================================

	private BookAddRequest buildValidRequest() {
		BookAddRequest request = new BookAddRequest();
		request.setName("Fundamentals of Programming");
		request.setDatePublished(LocalDateTime.of(2010, 2, 15, 12, 45));
		request.setPurchasePrice(20.0);
		request.setSalesPrice(35.0);
		request.setPublicationAmount(100);
		request.setAuthorIds(List.of(1));
		request.setStoreId(1);
		request.setGenreId(1);
		return request;
	}

	private void performPostAndExpect400(BookAddRequest request) throws Exception {
		mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))

				.andExpect(status().isBadRequest());

		// Service must NEVER be called if validation failed
		verify(bookService, never()).addBook(ArgumentMatchers.any());
	}
}