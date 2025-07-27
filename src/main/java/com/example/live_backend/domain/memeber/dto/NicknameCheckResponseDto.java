package com.example.live_backend.domain.memeber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameCheckResponseDto {
    
    private static final String AVAILABLE_MESSAGE = "사용 가능한 닉네임입니다.";
    private static final String UNAVAILABLE_MESSAGE = "이미 사용 중인 닉네임입니다.";
    
    private boolean available;
    private String message;
    
    public static NicknameCheckResponseDto available() {
        return new NicknameCheckResponseDto(true, AVAILABLE_MESSAGE);
    }
    
    public static NicknameCheckResponseDto unavailable() {
        return new NicknameCheckResponseDto(false, UNAVAILABLE_MESSAGE);
    }
} 