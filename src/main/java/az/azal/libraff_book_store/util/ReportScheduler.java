package az.azal.libraff_book_store.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import az.azal.libraff_book_store.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

	private final ReportExportService reportExportService;

	// Safe, persistent, cross-platform — no OS-specific checks needed
	private static final String REPORT_DIR = System.getProperty("user.home") + File.separator + "libraff-reports";

	@Scheduled(cron = "*/25 * * * * *")
	public void generateMonthlySalesReport() {
		saveReport(reportExportService::exportSalesPdf, "sales");
	}

	@Scheduled(cron = "*/25 * * * * *")
	public void generateMonthlyRestockReport() {
		saveReport(reportExportService::exportRestockPdf, "restock");
	}

	@Scheduled(cron = "*/25 * * * * *")
	public void generateMonthlyCombinedReport() {
		saveReport(reportExportService::exportCombinedPdf, "combined");
	}

	private void saveReport(PdfSupplier supplier, String type) {
		try {
			// 1. Create folder if it doesn't exist
			Path reportDir = Path.of(REPORT_DIR);
			if (!Files.exists(reportDir)) {
				Files.createDirectories(reportDir);
				log.info("Created reports directory at: {}", reportDir.toAbsolutePath());
			}

			// 2. Build file path inside the folder
			String fileName = String.format("report-%s-%s.pdf", type, LocalDate.now());
			Path filePath = reportDir.resolve(fileName);

			if (Files.exists(filePath)) {
				log.warn("Report already exists, skipping: {}", filePath.toAbsolutePath());
				return;
			}

			// 3. Write PDF
			byte[] pdf = supplier.get();
			Files.write(filePath, pdf);

			log.info("Report saved: {}", filePath.toAbsolutePath());

		} catch (Exception e) {
			log.error("Failed to generate {} report: {}", type, e.getMessage());
		}
	}

	@FunctionalInterface
	interface PdfSupplier {
		byte[] get() throws Exception;
	}
}