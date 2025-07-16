package com.example.live_backend.global.error.response;

import java.time.LocalDateTime;

import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseHandler<T> {
	private final LocalDateTime timestamp = LocalDateTime.now();
	private boolean success;
	private String message;
	private T data;
	private ErrorInfo error;
	
	private static final String SUCCESS_MESSAGE = "SUCCESS";

	public ResponseHandler(boolean success, String message, T data, ErrorInfo error) {
		this.success = success;
		this.message = message;
		this.data = data;
		this.error = error;
	}

	// 성공 응답
	public static <T> ResponseHandler<T> success(T data) {
		return ResponseHandler.<T>builder()
			.success(true)
			.message(SUCCESS_MESSAGE)
			.data(data)
			.error(null)
			.build();
	}

	// 에러 응답 (데이터 없음)
	public static <T> ResponseHandler<T> error(String errorCode, String message) {
		return error(errorCode, message, null);
	}

	// 에러 응답 (데이터 포함)
	public static <T> ResponseHandler<T> error(String errorCode, String message, T data) {
		return ResponseHandler.<T>builder()
			.success(false)
			.message("요청 처리 중 오류가 발생했습니다.")
			.data(data)
			.error(ErrorInfo.builder()
				.code(errorCode)
				.message(message)
				.build())
			.build();
	}

	public static <T> ResponseHandler<T> error(ErrorCode errorCode) {
		return error(errorCode.name(), errorCode.getDetail(), null);
	}

	public static <T> ResponseHandler<T> error(ErrorCode errorCode, T data) {
		return error(errorCode.name(), errorCode.getDetail(), data);
	}

	@Builder
	@Data
	public static class ErrorInfo {
		private String code;
		private String message;
	}
}
