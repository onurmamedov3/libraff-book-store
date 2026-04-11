package az.azal.libraff_book_store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import az.azal.libraff_book_store.enums.ErrorStatus;
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

	public MyException(ErrorStatus errorStatus) {
		super(errorStatus.getMessage());
		this.errorCode = errorStatus.getErrorCode();
		this.httpStatus = errorStatus.getHttpStatus();
	}

	// For writing custome messages in the response
	public MyException(String customMessage, ErrorStatus errorStatus) {
		super(customMessage);
		this.errorCode = errorStatus.getErrorCode();
		this.httpStatus = errorStatus.getHttpStatus();
	}

	public MyException(ErrorStatus errorStatus, BindingResult br) {
		super(errorStatus.getMessage());
		this.br = br;
		this.errorCode = errorStatus.getErrorCode();
		this.httpStatus = errorStatus.getHttpStatus();
	}

}
