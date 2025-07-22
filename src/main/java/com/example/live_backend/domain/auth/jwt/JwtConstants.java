package com.example.live_backend.domain.auth.jwt;


public final class JwtConstants {
    
    private JwtConstants() {
    }
    
    // 토큰 카테고리
    public static final String ACCESS_TOKEN_CATEGORY = "access_token";
    public static final String REFRESH_TOKEN_CATEGORY = "refresh_token";
    
    // HTTP 헤더
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    
    // 클레임 키
    public static final String CLAIM_CATEGORY = "category";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_OAUTH_ID = "oauthId";
    public static final String CLAIM_ROLE = "role";
} 