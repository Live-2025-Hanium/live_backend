package com.example.live_backend.domain.auth.jwt;

import lombok.Builder;
import lombok.Getter;

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