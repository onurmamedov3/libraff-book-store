package az.azal.libraff_book_store.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.AuthorEntity;
import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.entity.DiscountEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.AuthorRepository;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.repository.DiscountRepository;
import az.azal.libraff_book_store.repository.GenreRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.DiscountAddRequest;
import az.azal.libraff_book_store.response.DiscountAddResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountService {

	private final DiscountRepository repository;

	private final ModelMapper mapper;

	private final BookRepository bookRepository;

	private final AuthorRepository authorRepository;

	private final GenreRepository genreRepository;

	private final StoreRepository storeRepository;

	@Transactional
	public DiscountAddResponse createDiscount(DiscountAddRequest request) {

		// Check if start date is after end date
		if (request.getDiscountStartDate().isAfter(request.getDiscountEndDate())) {
			throw new MyException(ErrorStatus.INVALID_DATE_RANGE);
		}

		validateDiscountTargets(request);

		DiscountEntity discount = new DiscountEntity();
		mapper.map(request, discount);

		// Check validations
		if (request.getBookId() != null) {
			discount.setBook(bookRepository.findById(request.getBookId())
					.orElseThrow(() -> new MyException(ErrorStatus.BOOK_NOT_FOUND)));
		}

		if (request.getAuthorId() != null) {
			discount.setAuthor(authorRepository.findById(request.getAuthorId())
					.orElseThrow(() -> new MyException(ErrorStatus.AUTHOR_NOT_FOUND)));
		}

		if (request.getGenreId() != null) {
			discount.setGenre(genreRepository.findById(request.getGenreId())
					.orElseThrow(() -> new MyException(ErrorStatus.GENRE_NOT_FOUND)));
		}

		if (request.getStoreId() != null) {
			discount.setStore(storeRepository.findById(request.getStoreId())
					.orElseThrow(() -> new MyException(ErrorStatus.STORE_NOT_FOUND)));
		}

		discount.setIsActive(true);

		// Save discount record to the database
		repository.save(discount);

		// Create and return response
		DiscountAddResponse response = new DiscountAddResponse();

		mapper.map(discount, response);

		return response;
	}

	public Double calculateDiscountedPrice(BookEntity book, Integer storeId) {

		List<Integer> authorIds = book.getAuthors() != null
				? book.getAuthors().stream().map(AuthorEntity::getId).toList()
				: new ArrayList<>();

		Integer genreId = (book.getGenre() != null) ? book.getGenre().getId() : null;

		List<DiscountEntity> discounts = repository.findApplicableDiscounts(book.getId(), authorIds, genreId, storeId,
				LocalDate.now());

		if (discounts == null || discounts.isEmpty()) {
			return book.getSalesPrice();
		}

		Double discountedPercentage = discounts.get(0).getDiscountPercentage();

		Double rawPrice = book.getSalesPrice() * ((100 - discountedPercentage) / 100.0);

		return ((int) (rawPrice * 100)) / 100.0;
	}

	private void validateDiscountTargets(DiscountAddRequest request) {

		int targetCount = 0;

		if (request.getAuthorId() != null)
			targetCount++;

		if (request.getGenreId() != null)
			targetCount++;

		if (request.getBookId() != null)
			targetCount++;

		if (targetCount > 1) {
			throw new MyException(ErrorStatus.MULTIPLE_TARGETS);
		}

		if (targetCount == 0 && request.getStoreId() == null) {
			throw new MyException(ErrorStatus.NO_TARGET_SELECTED);
		}

	}

}
