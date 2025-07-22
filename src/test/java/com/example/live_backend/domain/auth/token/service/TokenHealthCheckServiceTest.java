package com.example.live_backend.domain.auth.token.service;

import com.example.live_backend.domain.auth.jwt.JwtUtil;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("토큰 상태 체크 서비스 테스트")
class TokenHealthCheckServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenHealthCheckService tokenHealthCheckService;

    private final String VALID_TOKEN = "valid.jwt.token";
    private final String EXPIRED_TOKEN = "expired.jwt.token";

    @Nested
    @DisplayName("토큰 상태 체크 기능")
    class HealthCheckTests {

                @Test
        @DisplayName("유효한 토큰 상태 체크 - 성공")
        void healthCheck_ValidToken_Success() {
            // Given
            com.example.live_backend.domain.auth.jwt.TokenInfo validTokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .build();
            given(jwtUtil.validateAndExtract(VALID_TOKEN)).willReturn(validTokenInfo);

            // When & Then
            assertThatCode(() -> tokenHealthCheckService.healthCheck(VALID_TOKEN))
                .doesNotThrowAnyException();
            
            verify(jwtUtil).validateAndExtract(VALID_TOKEN);
        }

        @Test
        @DisplayName("만료된 토큰 상태 체크 - 예외 발생")
        void healthCheck_ExpiredToken_ThrowsException() {
            // Given
            com.example.live_backend.domain.auth.jwt.TokenInfo expiredTokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(false)
                    .expired(true)
                    .build();
            
            given(jwtUtil.validateAndExtract(EXPIRED_TOKEN)).willReturn(expiredTokenInfo);

            // When & Then
            assertThatThrownBy(() -> tokenHealthCheckService.healthCheck(EXPIRED_TOKEN))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_TOKEN);
            
            verify(jwtUtil).validateAndExtract(EXPIRED_TOKEN);
        }

        @Test
        @DisplayName("null 토큰 상태 체크 - 예외 발생")
        void healthCheck_NullToken_ThrowsException() {
            // Given
            String nullToken = null;
            given(jwtUtil.validateAndExtract(nullToken)).willThrow(new IllegalArgumentException("Token cannot be null"));

            // When & Then
            assertThatThrownBy(() -> tokenHealthCheckService.healthCheck(nullToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
            
            verify(jwtUtil).validateAndExtract(nullToken);
        }

        @Test
        @DisplayName("빈 토큰 상태 체크 - 예외 발생")
        void healthCheck_EmptyToken_ThrowsException() {
            // Given
            String emptyToken = "";
            given(jwtUtil.validateAndExtract(emptyToken)).willThrow(new IllegalArgumentException("Token cannot be empty"));

            // When & Then
            assertThatThrownBy(() -> tokenHealthCheckService.healthCheck(emptyToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
            
            verify(jwtUtil).validateAndExtract(emptyToken);
        }
    }

    @Nested
    @DisplayName("다양한 토큰 시나리오 테스트")
    class VariousTokenScenarioTests {

        @Test
        @DisplayName("여러 유효한 토큰들 연속 체크 - 모두 성공")
        void healthCheck_MultipleValidTokens_AllSuccess() {
            // Given
            String token1 = "valid.token.1";
            String token2 = "valid.token.2";
            String token3 = "valid.token.3";
            
            com.example.live_backend.domain.auth.jwt.TokenInfo validTokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .build();
            
            given(jwtUtil.validateAndExtract(token1)).willReturn(validTokenInfo);
            given(jwtUtil.validateAndExtract(token2)).willReturn(validTokenInfo);
            given(jwtUtil.validateAndExtract(token3)).willReturn(validTokenInfo);

            // When & Then
            assertThatCode(() -> {
                tokenHealthCheckService.healthCheck(token1);
                tokenHealthCheckService.healthCheck(token2);
                tokenHealthCheckService.healthCheck(token3);
            }).doesNotThrowAnyException();
            
            verify(jwtUtil).validateAndExtract(token1);
            verify(jwtUtil).validateAndExtract(token2);
            verify(jwtUtil).validateAndExtract(token3);
        }

        @Test
        @DisplayName("유효한 토큰과 만료된 토큰 혼합 체크 - 만료된 토큰에서만 예외")
        void healthCheck_MixedTokens_ThrowsOnlyForExpired() {
            // Given
            String validToken = "valid.token";
            String expiredToken = "expired.token";
            
            com.example.live_backend.domain.auth.jwt.TokenInfo validTokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .build();
            
            com.example.live_backend.domain.auth.jwt.TokenInfo expiredTokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(false)
                    .expired(true)
                    .build();
            
            given(jwtUtil.validateAndExtract(validToken)).willReturn(validTokenInfo);
            given(jwtUtil.validateAndExtract(expiredToken)).willReturn(expiredTokenInfo);

            // When & Then
            assertThatCode(() -> tokenHealthCheckService.healthCheck(validToken))
                .doesNotThrowAnyException();
            
            assertThatThrownBy(() -> tokenHealthCheckService.healthCheck(expiredToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_TOKEN);
            
            verify(jwtUtil).validateAndExtract(validToken);
            verify(jwtUtil).validateAndExtract(expiredToken);
        }

        @Test
        @DisplayName("동일한 토큰 반복 체크 - 일관된 결과")
        void healthCheck_SameTokenMultipleTimes_ConsistentResult() {
            // Given
            com.example.live_backend.domain.auth.jwt.TokenInfo validTokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .build();
            
            given(jwtUtil.validateAndExtract(VALID_TOKEN)).willReturn(validTokenInfo);

            // When & Then
            for (int i = 0; i < 5; i++) {
                assertThatCode(() -> tokenHealthCheckService.healthCheck(VALID_TOKEN))
                    .doesNotThrowAnyException();
            }
            
            // JwtUtil.validateAndExtract가 5번 호출되었는지 확인
            verify(jwtUtil, org.mockito.Mockito.times(5)).validateAndExtract(VALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("에러 케이스 테스트")
    class ErrorCaseTests {

        @Test
        @DisplayName("JwtUtil에서 다른 예외 발생 시 - INVALID_TOKEN 예외로 변환")
        void healthCheck_JwtUtilThrowsOtherException_ConvertsToInvalidToken() {
            // Given
            RuntimeException originalException = new RuntimeException("JWT processing error");
            given(jwtUtil.validateAndExtract(VALID_TOKEN)).willThrow(originalException);

            // When & Then
            assertThatThrownBy(() -> tokenHealthCheckService.healthCheck(VALID_TOKEN))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
            
            verify(jwtUtil).validateAndExtract(VALID_TOKEN);
        }
    }
} 