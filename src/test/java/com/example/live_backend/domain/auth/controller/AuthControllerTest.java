package com.example.live_backend.domain.auth.controller;

import com.example.live_backend.domain.auth.dto.request.LogoutRequestDto;
import com.example.live_backend.domain.auth.jwt.JwtUtil;
import com.example.live_backend.domain.auth.service.AuthenticationFacade;
import com.example.live_backend.domain.auth.token.service.RefreshTokenService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 컨트롤러 테스트")
class AuthControllerTest {

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_REFRESH_TOKEN = "valid.refresh.token";

    @Nested
    @DisplayName("로그아웃 API 테스트")
    class LogoutTests {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 로그아웃 - 성공")
        void logout_ValidRefreshToken_Success() {
            // Given
            LogoutRequestDto request = createLogoutRequest(TEST_REFRESH_TOKEN);
            given(jwtUtil.getUserId(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
            doNothing().when(refreshTokenService).deleteRefreshToken(TEST_USER_ID);

            // When
            ResponseEntity<Void> response = authController.logout(request);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            verify(jwtUtil).getUserId(TEST_REFRESH_TOKEN);
            verify(refreshTokenService).deleteRefreshToken(TEST_USER_ID);
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 로그아웃 - 401 에러")
        void logout_InvalidRefreshToken_ThrowsUnauthorized() {
            // Given
            String invalidToken = "invalid.refresh.token";
            LogoutRequestDto request = createLogoutRequest(invalidToken);
            given(jwtUtil.getUserId(invalidToken)).willThrow(new RuntimeException("Invalid token"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

            verify(jwtUtil).getUserId(invalidToken);
            verify(refreshTokenService, never()).deleteRefreshToken(any());
        }

        @Test
        @DisplayName("만료된 리프레시 토큰으로 로그아웃 - 401 에러")
        void logout_ExpiredRefreshToken_ThrowsUnauthorized() {
            // Given
            String expiredToken = "expired.refresh.token";
            LogoutRequestDto request = createLogoutRequest(expiredToken);
            given(jwtUtil.getUserId(expiredToken)).willThrow(new RuntimeException("Token expired"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

            verify(jwtUtil).getUserId(expiredToken);
            verify(refreshTokenService, never()).deleteRefreshToken(any());
        }

        @Test
        @DisplayName("null 리프레시 토큰으로 로그아웃 - 401 에러")
        void logout_NullRefreshToken_ThrowsUnauthorized() {
            // Given
            LogoutRequestDto request = createLogoutRequest(null);
            given(jwtUtil.getUserId(null)).willThrow(new RuntimeException("Null token"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

            verify(jwtUtil).getUserId(null);
            verify(refreshTokenService, never()).deleteRefreshToken(any());
        }

        @Test
        @DisplayName("빈 리프레시 토큰으로 로그아웃 - 401 에러")
        void logout_EmptyRefreshToken_ThrowsUnauthorized() {
            // Given
            LogoutRequestDto request = createLogoutRequest("");
            given(jwtUtil.getUserId("")).willThrow(new RuntimeException("Empty token"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

            verify(jwtUtil).getUserId("");
            verify(refreshTokenService, never()).deleteRefreshToken(any());
        }

        @Test
        @DisplayName("리프레시 토큰 삭제 시 예외 발생해도 정상 처리")
        void logout_DeleteRefreshTokenThrowsException_StillSuccessful() {
            // Given
            LogoutRequestDto request = createLogoutRequest(TEST_REFRESH_TOKEN);
            given(jwtUtil.getUserId(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
            doThrow(new RuntimeException("Delete failed")).when(refreshTokenService).deleteRefreshToken(TEST_USER_ID);

            // When
            ResponseEntity<Void> response = authController.logout(request);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            verify(jwtUtil).getUserId(TEST_REFRESH_TOKEN);
            verify(refreshTokenService).deleteRefreshToken(TEST_USER_ID);
        }

        @Test
        @DisplayName("JWT 파싱 실패 시 INVALID_TOKEN 에러 발생")
        void logout_JwtParsingFailure_ThrowsInvalidToken() {
            // Given
            String malformedToken = "malformed.jwt.token";
            LogoutRequestDto request = createLogoutRequest(malformedToken);
            given(jwtUtil.getUserId(malformedToken)).willThrow(new RuntimeException("JWT parsing failed"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

            verify(jwtUtil).getUserId(malformedToken);
            verify(refreshTokenService, never()).deleteRefreshToken(any());
        }

        private LogoutRequestDto createLogoutRequest(String refreshToken) {
            return new LogoutRequestDto() {
                public String getRefreshToken() {
                    return refreshToken;
                }
            };
        }
    }
} 