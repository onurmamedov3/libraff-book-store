package az.azal.libraff_book_store.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportExportService {

	private final TransactionHistoryRepository transactionHistoryRepository;

	@Scheduled(cron = "0 0 0 1 * *")
	public byte[] exportTransactionsPdf() throws DocumentException, IOException {
		List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findAll();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();
		PdfWriter.getInstance(document, baos);
		document.open();

		// Title
		Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 20);
		document.add(new Paragraph("Transaction History Report", titleFont));
		document.add(new Paragraph(" ")); // spacer

		// Table — 8 columns matching your entity fields
		PdfPTable table = new PdfPTable(9);
		table.setWidthPercentage(100);

		// Headers
		table.addCell("ID");
		table.addCell("Book");
		table.addCell("Store");
		table.addCell("Employee");
		table.addCell("Quantity");
		table.addCell("Purchase Price");
		table.addCell("Sales Price");
		table.addCell("Transaction Type");
		table.addCell("Date");

		// Rows
		for (TransactionHistoryEntity t : transactions) {
			table.addCell(String.valueOf(t.getId()));
			table.addCell(t.getBook().getName()); // adjust to your BookEntity field
			table.addCell(t.getStore().getName()); // adjust to your StoreEntity field
			table.addCell(t.getEmployee().getName()); // adjust to your EmployeeEntity field
			table.addCell(String.valueOf(t.getQuantity()));
			table.addCell(String.format("$%.2f", t.getPurchasePrice()));
			table.addCell(String.format("$%.2f", t.getSalesPrice()));
			table.addCell(t.getTransactionType().name());
			table.addCell(t.getTransactionDate().toString());
		}

		document.add(table);
		document.close();

		return baos.toByteArray();
	}
}