package com.example.live_backend.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import com.example.live_backend.domain.auth.dto.AuthToken;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserInfoDto user;
    private boolean isNewUser;

    public static LoginResponseDto from(AuthUserDto user, AuthToken tokens) {
        UserInfoDto userInfo = UserInfoDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .role(user.getRole().name())
            .build();

        return LoginResponseDto.builder()
            .accessToken(tokens.accessToken())
            .refreshToken(tokens.refreshToken())
            .user(userInfo)
            .isNewUser(user.isNewUser())
            .build();
    }

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