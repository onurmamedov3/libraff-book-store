package az.azal.libraff_book_store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MyException extends RuntimeException {

	private BindingResult br;
	private String errorCode;
	private HttpStatus httpStatus;

	public MyException(String message, BindingResult br, String errorCode, HttpStatus httpStatus) {
		super(message);
		this.br = br;
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	public MyException(String message, String errorCode, HttpStatus httpStatus) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

}
