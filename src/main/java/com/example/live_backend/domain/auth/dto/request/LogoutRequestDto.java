package com.example.live_backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "로그아웃 요청 DTO")
public class LogoutRequestDto {
    
    @Schema(description = "리프레시 토큰", example = "eabcdefg12kj...", required = true)
    private String refreshToken;
} 