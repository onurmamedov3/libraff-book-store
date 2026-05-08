package az.azal.libraff_book_store.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordConfirmRequest {

	@NotBlank(message = "Reset token is required")
	@Pattern(regexp = "^[0-9a-fA-F\\-]{36}$", message = "Invalid reset token format")
	private String resetToken;

	@NotBlank(message = "New password is required")
	@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
	private String newPassword;
}