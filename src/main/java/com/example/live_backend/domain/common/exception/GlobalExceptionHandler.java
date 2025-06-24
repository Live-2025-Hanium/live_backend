package com.example.live_backend.domain.common.exception;

import com.example.live_backend.domain.common.response.Message;
import com.example.live_backend.domain.common.response.ResponseHandler;
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
    public ResponseEntity<?> handleCustomException(CustomException exception) {
        log.warn("[handleCustomException] : {} \n message: {}", exception.getErrorCode(),
                exception.getMessage());


        // Determine the HTTP status based on the error code
        HttpStatus status;
        switch (exception.getErrorCode()) {
            case USER_NOT_FOUND -> status = HttpStatus.NOT_FOUND;  // User not found
            default -> status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status)
                .body(ResponseHandler.errorResponse(exception.getMessage(), exception.getErrorCode().name()));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentException(MethodArgumentNotValidException exception) {
        List<FieldErrorResponse> errors = exception.getBindingResult().getFieldErrors()
                .stream().map(FieldErrorResponse::of)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseHandler.errorResponse(errors, Message.BAD_REQUEST.getDescription()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleServerException(Exception exception) {
        // TODO: ServerError log message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseHandler.errorResponse(exception.getMessage(), Message.SYSTEM_ERROR.getDescription()));
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
