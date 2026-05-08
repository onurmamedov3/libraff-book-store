package az.azal.libraff_book_store.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

	@NotBlank(message = "FIN is required")
	private String fin;

	@NotBlank(message = "OTP code is required")
	@Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
	private String otpCode;
}