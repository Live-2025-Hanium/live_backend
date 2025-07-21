package com.example.live_backend.domain.auth.jwt;

import lombok.Builder;
import lombok.Getter;

/**
 * JWT 토큰 검증 결과와 클레임 정보를 담는 클래스
 * 한 번의 파싱으로 모든 정보를 추출할 수 있도록 구현.
 */
@Getter
@Builder
public class TokenInfo {
    private final boolean valid;
    private final boolean expired;
    private final String category;
    private final Long userId;
    private final String oauthId;
    private final String role;
} 