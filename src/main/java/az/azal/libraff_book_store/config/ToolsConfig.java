package az.azal.libraff_book_store.config;

import az.azal.libraff_book_store.entity.BookEntity;
import az.azal.libraff_book_store.entity.BookStockEntity;
import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.EmployeeWorkHistoryEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.enums.TransactionType;
import az.azal.libraff_book_store.repository.BookRepository;
import az.azal.libraff_book_store.repository.BookStockRepository;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.EmployeeWorkHistoryRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ToolsConfig {

	private final EmployeeRepository employeeRepository;
	private final EmployeeWorkHistoryRepository workHistoryRepository;
	private final StoreRepository storeRepository;
	private final BookRepository bookRepository;
	private final BookStockRepository bookStockRepository;
	private final TransactionHistoryRepository transactionHistoryRepository;

	// ==================== İşçi Alətləri ====================

	/**
	 * AI-nin göndərəcəyi request formatı.
	 * record istifadə olunur çünki sadə, immutable, və Jackson tərəfindən
	 * avtomatik serialize/deserialize olunur.
	 */
	public record EmployeeRequest(
			@JsonProperty(required = true)
			@JsonPropertyDescription("Axtarılan işçinin adı")
			String employeeName) {
	}

	@Tool(description = "Verilən işçi adına görə cari maaşını, vəzifəsini və mağazasını məlumat bazasından gətirir")
	public String getEmployeeSalary(EmployeeRequest request) {
		String employeeName = request.employeeName();
		List<EmployeeEntity> allEmployees = employeeRepository.findAll();
		Optional<EmployeeEntity> employeeOpt = allEmployees.stream()
				.filter(e -> e.getName().equalsIgnoreCase(employeeName)
						|| (e.getName() + " " + e.getSurname()).equalsIgnoreCase(employeeName))
				.findFirst();

		if (employeeOpt.isEmpty()) {
			return "İşçi tapılmadı: " + employeeName;
		}

		EmployeeEntity employee = employeeOpt.get();

		List<EmployeeWorkHistoryEntity> histories = workHistoryRepository.findAll();
		Optional<EmployeeWorkHistoryEntity> activeHistory = histories.stream()
				.filter(h -> h.getEmployee().getId().equals(employee.getId())
						&& Boolean.TRUE.equals(h.getIsActive()))
				.findFirst();

		if (activeHistory.isEmpty()) {
			return employee.getName() + " " + employee.getSurname() + " — aktiv iş tarixçəsi yoxdur";
		}

		EmployeeWorkHistoryEntity h = activeHistory.get();
		return String.format("%s %s — Maaş: %.2f AZN, Vəzifə: %s, Mağaza: %s",
				employee.getName(), employee.getSurname(),
				h.getSalary(),
				h.getPosition() != null ? h.getPosition().getName() : "Bilinmir",
				h.getStore() != null ? h.getStore().getName() : "Bilinmir");
	}

	public record StoreRequest(
			@JsonProperty(required = true)
			@JsonPropertyDescription("Axtarılan mağazanın adı")
			String storeName) {
	}

	@Tool(description = "Verilən mağaza adına görə mağaza haqqında məlumat və işçi sayını gətirir")
	public String getStoreInfo(StoreRequest request) {
		String storeName = request.storeName();
		List<StoreEntity> allStores = storeRepository.findAll();
		Optional<StoreEntity> storeOpt = allStores.stream()
				.filter(s -> s.getName().toLowerCase().contains(storeName.toLowerCase()))
				.findFirst();

		if (storeOpt.isEmpty()) {
			return "Mağaza tapılmadı: " + storeName;
		}

		StoreEntity store = storeOpt.get();
		long employeeCount = store.getEmployees() != null ? store.getEmployees().size() : 0;

		return String.format("Mağaza: %s, Ünvan: %s, Aktiv işçi sayı: %d",
				store.getName(),
				store.getAddress() != null ? store.getAddress() : "Ünvan yoxdur",
				employeeCount);
	}

	public record BookRequest(
			@JsonProperty(required = true)
			@JsonPropertyDescription("Axtarılan kitabın adı")
			String bookName) {
	}

	@Tool(description = "Verilən kitab adına görə kitabın qiyməti, müəllifi və stok məlumatını gətirir")
	public String getBookInfo(BookRequest request) {
		String bookName = request.bookName();
		List<BookEntity> allBooks = bookRepository.findAll();
		Optional<BookEntity> bookOpt = allBooks.stream()
				.filter(b -> b.getName().toLowerCase().contains(bookName.toLowerCase()))
				.findFirst();

		if (bookOpt.isEmpty()) {
			return "Kitab tapılmadı: " + bookName;
		}

		BookEntity book = bookOpt.get();

		List<BookStockEntity> stocks = bookStockRepository.findAll();
		int totalStock = stocks.stream()
				.filter(s -> s.getBook().getId().equals(book.getId()))
				.mapToInt(BookStockEntity::getQuantity)
				.sum();

		String authors = book.getAuthors() != null && !book.getAuthors().isEmpty()
				? book.getAuthors().stream()
						.map(a -> a.getName() + " " + a.getSurname())
						.reduce((a, b) -> a + ", " + b)
						.orElse("Bilinmir")
				: "Bilinmir";

		return String.format("Kitab: %s, Müəllif: %s, Alış qiyməti: %.2f AZN, Satış qiyməti: %.2f AZN, Ümumi stok: %d",
				book.getName(),
				authors,
				book.getPurchasePrice() != null ? book.getPurchasePrice() : 0.0,
				book.getSalesPrice() != null ? book.getSalesPrice() : 0.0,
				totalStock);
	}

	@Tool(description = "Verilən mağaza adına görə bu ayki ümumi satış məbləğini gətirir")
	public String getStoreSales(StoreRequest request) {
		String storeName = request.storeName();
		List<StoreEntity> allStores = storeRepository.findAll();
		Optional<StoreEntity> storeOpt = allStores.stream()
				.filter(s -> s.getName().toLowerCase().contains(storeName.toLowerCase()))
				.findFirst();

		if (storeOpt.isEmpty()) {
			return "Mağaza tapılmadı: " + storeName;
		}

		StoreEntity store = storeOpt.get();
		YearMonth currentMonth = YearMonth.now();
		LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
		LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

		List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findAll();
		double totalSales = transactions.stream()
				.filter(t -> t.getStore().getId().equals(store.getId())
						&& t.getTransactionType() == TransactionType.SALE
						&& t.getTransactionDate().isAfter(monthStart)
						&& t.getTransactionDate().isBefore(monthEnd))
				.mapToDouble(t -> t.getSalesPrice() * t.getQuantity())
				.sum();

		return String.format("Mağaza: %s, %s %d satışları: %.2f AZN",
				store.getName(),
				currentMonth.getMonth().name(),
				currentMonth.getYear(),
				totalSales);
	}
}
