package az.azal.libraff_book_store.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import az.azal.libraff_book_store.service.ReportExportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

	private final ReportExportService exportService;

	private final TransactionHistoryRepository transactionHistoryRepository;

	@GetMapping("/transactions/pdf")
	public ResponseEntity<byte[]> exportPdf() throws Exception {
		byte[] pdf = exportService.exportTransactionsPdf();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "transactions.pdf");

		return ResponseEntity.ok().headers(headers).body(pdf);
	}

	@GetMapping("/transactions/csv")
	public ResponseEntity<byte[]> exportCsv() {
		List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findAll();

		StringBuilder csv = new StringBuilder(
				"ID,Book,Store,Employee,Quantity,Purchase Price,Sales Price,Transaction Type,Date\n");

		for (TransactionHistoryEntity t : transactions) {
			csv.append(t.getId()).append(",").append(t.getBook().getName()).append(",").append(t.getStore().getName())
					.append(",").append(t.getEmployee().getName()).append(",").append(t.getQuantity()).append(",")
					.append(t.getPurchasePrice()).append(",").append(t.getSalesPrice()).append(",")
					.append(t.getTransactionType().name()).append(",").append(t.getTransactionDate()).append("\n");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("text/csv"));
		headers.setContentDispositionFormData("attachment", "transactions.csv");

		return ResponseEntity.ok().headers(headers).body(csv.toString().getBytes());
	}
}