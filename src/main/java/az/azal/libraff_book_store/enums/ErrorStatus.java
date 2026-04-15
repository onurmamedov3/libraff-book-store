package az.azal.libraff_book_store.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus {

	DUPLICATE_FIN("Employee with this FIN already exists!", "DUPLICATE_FIN", HttpStatus.CONFLICT),

	DUPLICATE_PHONE("Employee with this Phone number already exists!", "DUPLICATE_PHONE", HttpStatus.CONFLICT),

	DUPLICATE_EMAIL("Employee with this Email already exists!", "DUPLICATE_EMAIL", HttpStatus.CONFLICT),

	STORE_NOT_FOUND("Store not found", "NOT_FOUND", HttpStatus.NOT_FOUND),

	MANAGER_NOT_FOUND("Manager not found", "MANAGER_NOT_FOUND", HttpStatus.NOT_FOUND),

	AUTHOR_NOT_FOUND("One or more authors not found!", "AUTHOR_NOT_FOUND", HttpStatus.NOT_FOUND),

	GENRE_NOT_FOUND("Genre not found!", "GENRE_NOT_FOUND", HttpStatus.NOT_FOUND),

	INVALID_SALARY("This salary is not valid for the new position range!", "INVALID_SALARY", HttpStatus.BAD_REQUEST),

	EMPLOYEE_NOT_FOUND("Employee not found!", "EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND),

	IMMUTABLE_FIELD("This field is immutable and cannot be changed!", "IMMUTABLE_FIELD", HttpStatus.BAD_REQUEST),

	POSITION_NOT_FOUND("Position not found", "POSITION_NOT_FOUND", HttpStatus.NOT_FOUND),

	ALREADY_INACTIVE("Employee is already inactive!", "ALREADY_INACTIVE", HttpStatus.CONFLICT),

	EMPLOYEE_INACTIVE("Employee is inactive!", "EMPLOYEE_INACTIVE", HttpStatus.CONFLICT),

	POSITION_LIMIT_EXCEEDED("Cannot add employee. The limit for this position in the selected store has been reached.",
			"POSITION_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST),

	UNAUTHORIZED_OPERATION("Unemployed employees cannot perform this operation!", "UNAUTHORIZED_OPERATION",
			HttpStatus.BAD_REQUEST),

	INVALID_DATE_RANGE("Start date cannot be after the end date!", "INVALID_DATE_RANGE", HttpStatus.BAD_REQUEST),

	MULTIPLE_TARGETS("You can only select one target (Book, Author, or Genre) at a time!", "MULTIPLE_TARGETS",
			HttpStatus.BAD_REQUEST),

	NO_TARGET_SELECTED("No target has been chosen!", "NO_TARGET_SELECTED", HttpStatus.BAD_REQUEST),

	BOOK_NOT_FOUND("Book not found in this store!", "BOOK_NOT_FOUND", HttpStatus.NOT_FOUND),

	NOT_ENOUGH_STOCK("Not enough stock for the requested book!", "NOT_ENOUGH_ITEM", HttpStatus.BAD_REQUEST),

	INVALID_OPERATION("Invalid operatoin", "INVALID_OPERATION", HttpStatus.BAD_REQUEST),

	STOCK_NOT_FOUND("Stock not found", "STOCK_NOT_FOUND", HttpStatus.INTERNAL_SERVER_ERROR),

	INSUFFICIENT_STOCK("Insufficient stock", "INSUFFICIENT_STOCK", HttpStatus.BAD_REQUEST),

	INVALID_STATUS("Invalid status", "INVALID_STATUS", HttpStatus.BAD_REQUEST),

	VALIDATION_ERROR("Validation failed", "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);

	private final String message;
	private final String errorCode;
	private final HttpStatus httpStatus;

}
