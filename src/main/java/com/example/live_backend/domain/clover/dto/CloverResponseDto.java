package com.example.live_backend.domain.clover.dto;

public record CloverResponseDto(int cloverCount) {

    public static CloverResponseDto of(int cloverCount) {
        return new CloverResponseDto(cloverCount);
    }
}
