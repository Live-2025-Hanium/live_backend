package com.example.live_backend.domain.common.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseHandler<T> {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private String message;
    private T data;

    public ResponseHandler(final String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseHandler<T> response(T data) {
        return ResponseHandler.<T>builder()
                .message(Message.SUCCESS.getDescription())
                .data(data)
                .build();
    }

    public static <T> ResponseHandler<T> errorResponse(T data, String message) {
        return ResponseHandler.<T>builder()
                .message(message)
                .data(data)
                .build();
    }
}
