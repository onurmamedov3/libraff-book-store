package az.azal.libraff_book_store.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.response.LowStockBookDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${MAIL_USERNAME}")
	private String senderEmail;

	public void sendLowStockNotification(Map<String, List<LowStockBookDTO>> lowStockBooks) {

		for (Map.Entry<String, List<LowStockBookDTO>> entry : lowStockBooks.entrySet()) {

			String managerEmail = entry.getKey();

			List<LowStockBookDTO> books = entry.getValue();

			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(senderEmail);
			message.setTo(managerEmail);
			message.setSubject("Low stock alert!");
			message.setText(buildEmailBody(books));

			try {
				mailSender.send(message);
				log.info("Low stock email sent to {}", managerEmail);
			} catch (MailException e) {
				log.error("Failed to send email to {}", managerEmail, e);
			}
		}

	}

	private String buildEmailBody(List<LowStockBookDTO> books) {

		StringBuilder sb = new StringBuilder();
		sb.append("The following books are running low on stock:\n\n");
		for (LowStockBookDTO book : books) {
			sb.append("The book name: ").append(book.getName()).append("\n");
			sb.append("The quantity left: ").append(book.getQuantity()).append("\n");
			sb.append("The store name: ").append(book.getStoreName()).append("\n");
			sb.append("*********************************\n");
		}

		return sb.toString();
	}

}
