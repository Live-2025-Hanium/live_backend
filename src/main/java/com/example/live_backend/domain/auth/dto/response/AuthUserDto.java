package com.example.live_backend.domain.auth.dto.response;

import com.example.live_backend.domain.memeber.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthUserDto {
    private Long id;
    private String kakaoId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Role role;
    private boolean isNewUser;
} 