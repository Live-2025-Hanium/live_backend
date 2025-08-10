package com.example.live_backend.config;

import com.example.live_backend.domain.auth.jwt.JwtTokenValidator;
import com.example.live_backend.domain.auth.jwt.TokenInfo;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestTokenServiceConfig {

    @Bean
    @Primary
    public JwtTokenValidator jwtTokenValidator() {
        JwtTokenValidator mock = mock(JwtTokenValidator.class);
        
        // 관리자용 TokenInfo
        TokenInfo adminTokenInfo = mock(TokenInfo.class);
        when(adminTokenInfo.getUserId()).thenReturn(1L);
        when(adminTokenInfo.getRole()).thenReturn("ADMIN");
        when(adminTokenInfo.isExpired()).thenReturn(false);
        
        // 일반 사용자용 TokenInfo
        TokenInfo userTokenInfo = mock(TokenInfo.class);
        when(userTokenInfo.getUserId()).thenReturn(2L);
        when(userTokenInfo.getRole()).thenReturn("USER");
        when(userTokenInfo.isExpired()).thenReturn(false);
        
        // validateAccessToken 메서드 모킹
        when(mock.validateAccessToken("admin-token")).thenReturn(adminTokenInfo);
        when(mock.validateAccessToken("user-token")).thenReturn(userTokenInfo);
        when(mock.validateAccessToken(anyString())).thenReturn(userTokenInfo); // 기본값
        
        return mock;
    }
} 