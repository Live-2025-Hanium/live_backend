package com.example.live_backend.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserInfoDto user;
    private boolean isNewUser;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private Long id;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private String role;
    }
} 