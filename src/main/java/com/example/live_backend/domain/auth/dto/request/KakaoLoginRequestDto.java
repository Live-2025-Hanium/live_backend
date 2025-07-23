package com.example.live_backend.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoLoginRequestDto {
    private String kakaoId;
    private String email;
    private String nickname;
    private String profileImageUrl;
} 