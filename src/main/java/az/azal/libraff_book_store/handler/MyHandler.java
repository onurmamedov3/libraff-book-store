package az.azal.libraff_book_store.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.response.MyErrorResponse;
import az.azal.libraff_book_store.response.MyFieldError;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class MyHandler {

	@ExceptionHandler
	public ResponseEntity<MyErrorResponse> handleMyException(MyException e, HttpServletRequest request) {
		MyErrorResponse resp = new MyErrorResponse();
		BindingResult br = e.getBr();

		// HttpStatus status = (e.getHttpStatus() != null) ? e.getHttpStatus() :
		// HttpStatus.BAD_REQUEST;

		if (br != null) {
			List<FieldError> fieldErrors = br.getFieldErrors();
			List<MyFieldError> myList = new ArrayList<MyFieldError>();
			for (FieldError error : fieldErrors) {
				MyFieldError myErr = new MyFieldError();
				myErr.setField(error.getField());
				myErr.setMessage(error.getDefaultMessage());
				myList.add(myErr);
			}

			resp.setValidations(myList);

		}
		resp.setGuid(UUID.randomUUID().toString());

		resp.setErrorCode(e.getErrorCode());
		resp.setMessage(e.getMessage());
		resp.setStatusCode(e.getHttpStatus().value());
		resp.setStatusName(e.getHttpStatus().name());
		resp.setPath(request.getRequestURI());
		resp.setMethod(request.getMethod());
		resp.setTimestamp(LocalDateTime.now());

		return new ResponseEntity(resp, e.getHttpStatus());

	}

//	@ExceptionHandler(AccessDeniedException.class)
//	public ResponseEntity<MyErrorResponse> handleAccessDeniedException(AccessDeniedException e,
//			HttpServletRequest request) {
//		MyErrorResponse resp = createBaseResponse(request, "ACCESS_DENIED",
//				"You do not have permission to perform this action.");
//		return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
//	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<MyErrorResponse> handleGeneralException(Exception e, HttpServletRequest request) {
		MyErrorResponse resp = createBaseResponse(request, "INTERNAL_SERVER_ERROR",
				"An unexpected error occurred on the server.");
		return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private MyErrorResponse createBaseResponse(HttpServletRequest request, String code, String msg) {
		MyErrorResponse resp = new MyErrorResponse();
		resp.setGuid(UUID.randomUUID().toString());
		resp.setErrorCode(code);
		resp.setMessage(msg);
		resp.setPath(request.getRequestURI());
		resp.setMethod(request.getMethod());
		resp.setTimestamp(LocalDateTime.now());
		return resp;

	}
}
