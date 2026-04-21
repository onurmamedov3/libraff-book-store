package az.azal.libraff_book_store.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.response.MyErrorResponse;
import az.azal.libraff_book_store.response.MyFieldError;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class MyHandler {

	private static final Logger log = LoggerFactory.getLogger(MyHandler.class);

	@ExceptionHandler
	public ResponseEntity<MyErrorResponse> handleMyException(MyException e, HttpServletRequest request) {
		MyErrorResponse resp = new MyErrorResponse();
		BindingResult br = e.getBr();

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

		return new ResponseEntity<>(resp, e.getHttpStatus());

	}

//	@ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
//	public ResponseEntity<MyErrorResponse> handleAccessDeniedException(Exception e, HttpServletRequest request) {
//
//		String errorMessage = "Access Denied: You do not have the required permissions or roles to perform this action.";
//
//		log.warn("Unauthorized access attempt on {} - {}", request.getRequestURI(), e.getMessage());
//
//		MyErrorResponse resp = createBaseResponse(request, "ACCESS_DENIED", errorMessage, HttpStatus.FORBIDDEN);
//
//		return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
//	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<MyErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
			HttpServletRequest request) {

		String errorMessage = "Malformed JSON request. Please check your JSON syntax and ensure all fields match their expected data types (e.g., correct text, numbers, or specific Enum values).";

		log.warn("JSON Parse Error on {}: {}", request.getRequestURI(), e.getMostSpecificCause().getMessage());

		MyErrorResponse resp = createBaseResponse(request, "INVALID_JSON_FORMAT", errorMessage, HttpStatus.BAD_REQUEST);

		return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<MyErrorResponse> handleGeneralException(Exception e, HttpServletRequest request) {

		log.error("Unexpected error on {}", request.getRequestURI(), e);

		MyErrorResponse resp = createBaseResponse(request, "INTERNAL_SERVER_ERROR",
				"An unexpected error occurred on the server.", HttpStatus.INTERNAL_SERVER_ERROR);

		return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<MyErrorResponse> handleRequestNotPermitted(RequestNotPermitted e,
			HttpServletRequest request) {

		log.error("Too many requests {}", request.getRequestURI(), e);

		MyErrorResponse resp = createBaseResponse(request, "TOO_MANY_REQUESTS",
				"RateLimiter does not permit further calls.", HttpStatus.TOO_MANY_REQUESTS);

		return new ResponseEntity<>(resp, HttpStatus.TOO_MANY_REQUESTS);
	}

	private MyErrorResponse createBaseResponse(HttpServletRequest request, String code, String msg, HttpStatus status) {
		MyErrorResponse resp = new MyErrorResponse();
		resp.setGuid(UUID.randomUUID().toString());
		resp.setErrorCode(code);
		resp.setMessage(msg);
		resp.setStatusCode(status.value());
		resp.setStatusName(status.name());
		resp.setPath(request.getRequestURI());
		resp.setMethod(request.getMethod());
		resp.setTimestamp(LocalDateTime.now());
		return resp;

	}

}
