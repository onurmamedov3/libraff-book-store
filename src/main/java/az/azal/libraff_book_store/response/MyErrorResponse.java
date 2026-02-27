package az.azal.libraff_book_store.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Data;

@Data
public class MyErrorResponse {

	private List<MyFieldError> validations;
	private String guid;
	private String errorCode;
	private String message;
	private Integer statusCode;
	private String statusName;
	private String path;
	private String method;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime timestamp;

}
