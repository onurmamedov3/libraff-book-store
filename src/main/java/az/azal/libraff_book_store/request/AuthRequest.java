package az.azal.libraff_book_store.request;

import lombok.Data;

@Data
public class AuthRequest {

	private String FIN;

	private String password;
}