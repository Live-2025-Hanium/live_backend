package com.example.live_backend.global.error.exception;

import com.example.live_backend.global.error.response.ResponseHandler;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ResponseHandler<Object>> handleCustomException(CustomException exception) {
		log.warn("[handleCustomException] : {} \n message: {}", exception.getErrorCode(),
			exception.getMessage());

		HttpStatus status = exception.getErrorCode().getHttpStatus();

		ResponseHandler<Object> response = ResponseHandler.error(exception.getErrorCode());

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseHandler<List<FieldErrorResponse>>> handleMethodArgumentException(
		MethodArgumentNotValidException exception) {

		List<FieldErrorResponse> errors = exception.getBindingResult().getFieldErrors()
			.stream().map(FieldErrorResponse::of)
			.toList();

		ResponseHandler<List<FieldErrorResponse>> response = ResponseHandler.error(
			ErrorCode.VALIDATION_FAILED,
			errors
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ResponseHandler<Object>> handleServerException(Exception exception) {
		log.error("[handleServerException] : {}", exception.getMessage(), exception);

		ResponseHandler<Object> response = ResponseHandler.error(ErrorCode.INTERNAL_SERVER_ERROR);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	private record FieldErrorResponse(String field, String message, Object rejectedValue) {
		public static FieldErrorResponse of(final FieldError fieldError) {
			return new FieldErrorResponse(
				fieldError.getField(),
				fieldError.getDefaultMessage(),
				fieldError.getRejectedValue()
			);
		}
	}
}
