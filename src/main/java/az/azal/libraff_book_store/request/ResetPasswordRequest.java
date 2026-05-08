package az.azal.libraff_book_store.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "FIN is required")
    @Pattern(regexp = "^[A-Za-z0-9]{7}$", message = "FIN must be exactly 7 alphanumeric characters")
    private String fin;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}