package az.azal.libraff_book_store.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.enums.TransactionType;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportExportService {

	private final TransactionHistoryRepository transactionHistoryRepository;

	private LocalDateTime[] getCurrentMonthRange() {
		LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
		// atStartOfDay() → 2026-04-01T00:00:00

		LocalDateTime end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
		// atTime(23, 59, 59) → 2026-04-30T23:59:59

		return new LocalDateTime[] { start, end };
	}

	@Transactional(readOnly = true)
	public byte[] exportSalesPdf() throws Exception {
		LocalDateTime[] range = getCurrentMonthRange();
		List<TransactionHistoryEntity> sales = transactionHistoryRepository
				.findByTransactionTypeAndTransactionDateBetween(TransactionType.SALE, range[0], range[1]);
		return buildPdf("Monthly Sales Report", sales, range[0], range[1]);
	}

	@Transactional(readOnly = true)
	public byte[] exportRestockPdf() throws Exception {
		LocalDateTime[] range = getCurrentMonthRange();
		List<TransactionHistoryEntity> restocks = transactionHistoryRepository
				.findByTransactionTypeAndTransactionDateBetween(TransactionType.RESTOCK, range[0], range[1]);
		return buildPdf("Monthly Restock Report", restocks, range[0], range[1]);
	}

	@Transactional(readOnly = true)
	public byte[] exportCombinedPdf() throws Exception {
		LocalDateTime[] range = getCurrentMonthRange();
		List<TransactionHistoryEntity> all = transactionHistoryRepository.findByTransactionDateBetween(range[0],
				range[1]);
		return buildPdf("Monthly Full Report (Sales + Restock)", all, range[0], range[1]);
	}

	private byte[] buildPdf(String title, List<TransactionHistoryEntity> transactions, LocalDateTime start,
			LocalDateTime end) throws Exception { // ✅ LocalDateTime
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();
		PdfWriter.getInstance(document, baos);
		document.open();

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		document.add(new Paragraph(title, titleFont));

		Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
		document.add(new Paragraph("Period    : " + start.format(dateFormatter) + " to " + end.format(dateFormatter),
				subFont));
		document.add(new Paragraph("Generated : " + LocalDate.now().format(dateFormatter), subFont));
		document.add(new Paragraph(" "));

		if (transactions.isEmpty()) {
			document.add(new Paragraph("No transactions found for this period."));
			document.close();
			return baos.toByteArray();
		}

		PdfPTable table = new PdfPTable(9);
		table.setWidthPercentage(100);

		for (String header : new String[] { "ID", "Book", "Store", "Employee", "Quantity", "Purchase Price",
				"Sales Price", "Type", "Date" }) {
			table.addCell(header);
		}

		double grandTotal = 0;
		for (TransactionHistoryEntity t : transactions) {
			double lineTotal = t.getSalesPrice() * t.getQuantity();
			grandTotal += lineTotal;

			table.addCell(String.valueOf(t.getId()));
			table.addCell(t.getBook().getName());
			table.addCell(t.getStore().getName());
			table.addCell(t.getEmployee().getName());
			table.addCell(String.valueOf(t.getQuantity()));
			table.addCell(String.format(Locale.US, "$%.2f", t.getPurchasePrice()));
			table.addCell(String.format(Locale.US, "$%.2f", t.getSalesPrice()));
			table.addCell(t.getTransactionType().name());
			table.addCell(t.getTransactionDate().format(dateFormatter));
		}

		document.add(table);
		document.add(new Paragraph(" "));

		Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
		document.add(new Paragraph("Grand Total: " + String.format(Locale.US, "$%.2f", grandTotal), totalFont));

		document.close();
		return baos.toByteArray();
	}
}