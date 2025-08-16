package com.example.live_backend.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private UserInfoDto user;
    private boolean isNewUser;
    private String accessToken;
    private String refreshToken;

    public static LoginResponseDto from(AuthUserDto user, String accessToken, String refreshToken) {
        UserInfoDto userInfo = UserInfoDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .role(user.getRole().name())
            .build();

        return LoginResponseDto.builder()
            .user(userInfo)
            .isNewUser(user.isNewUser())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
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