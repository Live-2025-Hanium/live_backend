package com.example.live_backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Message {
    SUCCESS("SUCCESS"),
    BAD_REQUEST("BAD REQUEST"),
    SYSTEM_ERROR("SYSTEM ERROR");

    private final String description;
}
