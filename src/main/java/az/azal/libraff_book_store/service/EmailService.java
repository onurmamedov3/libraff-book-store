package az.azal.libraff_book_store.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.response.LowStockBookDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${MAIL_USERNAME}")
	private String senderEmail;

	@Value("${spring.mail.password}")
	private String mailPassword;

	public void sendLowStockNotification(Map<String, List<LowStockBookDTO>> lowStockBooks) {

		System.out.println("EMAIL: " + senderEmail);
		System.out.println("PASS LENGTH: " + mailPassword.length());

		for (Map.Entry<String, List<LowStockBookDTO>> entry : lowStockBooks.entrySet()) {
			String managerEmail = entry.getKey();
			List<LowStockBookDTO> books = entry.getValue();

			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(managerEmail);
			message.setTo(managerEmail);
			message.setSubject("Low stock alert!");
			message.setText(buildEmailBody(books));

			mailSender.send(message);
		}

	}

	private String buildEmailBody(List<LowStockBookDTO> books) {

		StringBuilder sb = new StringBuilder();
		sb.append("The following books are running low on stock:\n\n");
		for (LowStockBookDTO book : books) {
			sb.append("The book: ").append(book.getName());
			sb.append("The quantity: ").append(book.getQuantity());
			sb.append("The store name: ").append(book.getStoreName()).append("\n");

		}

		return sb.toString();
	}

}
