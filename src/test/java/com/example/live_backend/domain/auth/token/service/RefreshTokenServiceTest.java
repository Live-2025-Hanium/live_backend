package com.example.live_backend.domain.auth.token.service;

import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.util.AuthTokenGenerator;
import com.example.live_backend.domain.auth.jwt.JwtTokenValidator;
import com.example.live_backend.domain.auth.token.entity.RefreshToken;
import com.example.live_backend.domain.auth.token.repository.RefreshTokenRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("리프레시 토큰 서비스 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    // 분리 후: JwtTokenValidator를 Mock으로 사용
    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private AuthTokenGenerator generator;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_OAUTH_ID = "oauth123";
    private final String TEST_ROLE = "USER";
    private final String TEST_REFRESH_TOKEN = "valid.refresh.token";
    private final String NEW_ACCESS_TOKEN = "new.access.token";
    private final String NEW_REFRESH_TOKEN = "new.refresh.token";

    @Nested
    @DisplayName("리프레시 토큰 조회 기능")
    class GetRefreshTokenTests {

        @Test
        @DisplayName("존재하는 사용자의 리프레시 토큰 조회 - 성공")
        void getUserRefreshToken_ExistingUser_Success() {
            // Given
            RefreshToken mockRefreshToken = new RefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN);
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.of(mockRefreshToken));

            // When
            RefreshToken result = refreshTokenService.getUserRefreshToken(TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getToken()).isEqualTo(TEST_REFRESH_TOKEN);
            verify(repository).findById(TEST_USER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 리프레시 토큰 조회 - 예외 발생")
        void getUserRefreshToken_NonExistingUser_ThrowsException() {
            // Given
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getUserRefreshToken(TEST_USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISSING_REFRESH_TOKEN);
            verify(repository).findById(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("토큰 리프레시 기능")
    class RefreshTests {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새 토큰 발급 - 성공")
        void refresh_ValidToken_Success() {
            // Given
            RefreshToken mockRefreshToken = new RefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN);
            AuthToken newTokens = new AuthToken(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);
            
            com.example.live_backend.domain.auth.jwt.TokenInfo tokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .category("refresh_token")
                    .userId(TEST_USER_ID)
                    .oauthId(TEST_OAUTH_ID)
                    .role(TEST_ROLE)
                    .build();
            
            // 분리 후: JwtTokenValidator의 validateRefreshToken 메서드 Mock
            given(jwtTokenValidator.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(tokenInfo);
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.of(mockRefreshToken));
            given(generator.generate(TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE)).willReturn(newTokens);

            // When
            AuthToken result = refreshTokenService.refresh(TEST_REFRESH_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            
            verify(jwtTokenValidator).validateRefreshToken(TEST_REFRESH_TOKEN);
            verify(generator).generate(TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE);
        }

        @Test
        @DisplayName("만료된 리프레시 토큰으로 새 토큰 발급 - 예외 발생")
        void refresh_ExpiredToken_ThrowsException() {
            // Given
            // 분리 후: JwtTokenValidator에서 예외를 던지도록 Mock 설정
            given(jwtTokenValidator.validateRefreshToken(TEST_REFRESH_TOKEN))
                .willThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.refresh(TEST_REFRESH_TOKEN))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_TOKEN);
            
            verify(jwtTokenValidator).validateRefreshToken(TEST_REFRESH_TOKEN);
            verifyNoInteractions(repository, generator);
        }

        @Test
        @DisplayName("저장된 토큰과 다른 리프레시 토큰으로 새 토큰 발급 - 예외 발생")
        void refresh_MismatchedToken_ThrowsException() {
            // Given
            String differentToken = "different.refresh.token";
            RefreshToken mockRefreshToken = new RefreshToken(TEST_USER_ID, differentToken);
            
            com.example.live_backend.domain.auth.jwt.TokenInfo tokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .category("refresh_token")
                    .userId(TEST_USER_ID)
                    .oauthId(TEST_OAUTH_ID)
                    .role(TEST_ROLE)
                    .build();
            
            given(jwtTokenValidator.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(tokenInfo);
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.of(mockRefreshToken));

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.refresh(TEST_REFRESH_TOKEN))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
            
            verify(jwtTokenValidator).validateRefreshToken(TEST_REFRESH_TOKEN);
            verify(repository).findById(TEST_USER_ID);
            verifyNoInteractions(generator);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 리프레시 토큰으로 새 토큰 발급 - 예외 발생")
        void refresh_NonExistingUser_ThrowsException() {
            // Given
            com.example.live_backend.domain.auth.jwt.TokenInfo tokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .category("refresh_token")
                    .userId(TEST_USER_ID)
                    .oauthId(TEST_OAUTH_ID)
                    .role(TEST_ROLE)
                    .build();
            
            given(jwtTokenValidator.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(tokenInfo);
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.refresh(TEST_REFRESH_TOKEN))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISSING_REFRESH_TOKEN);
            
            verify(jwtTokenValidator).validateRefreshToken(TEST_REFRESH_TOKEN);
            verify(repository).findById(TEST_USER_ID);
            verifyNoInteractions(generator);
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 저장 기능")
    class SaveRefreshTokenTests {

        @Test
        @DisplayName("새로운 사용자의 리프레시 토큰 저장 - 성공")
        void saveRefreshToken_NewUser_Success() {
            // Given
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.empty());

            // When
            refreshTokenService.saveRefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN);

            // Then
            verify(repository).findById(TEST_USER_ID);
            verify(repository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("기존 사용자의 리프레시 토큰 업데이트 - 성공")
        void saveRefreshToken_ExistingUser_Success() {
            // Given
            RefreshToken existingToken = spy(new RefreshToken(TEST_USER_ID, "old.token"));
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.of(existingToken));

            // When
            refreshTokenService.saveRefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN);

            // Then
            verify(repository).findById(TEST_USER_ID);
            verify(existingToken).updateToken(TEST_REFRESH_TOKEN);
            verify(repository, never()).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 삭제 기능")
    class DeleteRefreshTokenTests {

        @Test
        @DisplayName("사용자의 리프레시 토큰 삭제 - 성공")
        void deleteRefreshToken_Success() {
            // When
            refreshTokenService.deleteRefreshToken(TEST_USER_ID);

            // Then
            verify(repository).deleteById(TEST_USER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 리프레시 토큰 삭제 - 정상 처리")
        void deleteRefreshToken_NonExistingUser_NoException() {
            // Given
            doNothing().when(repository).deleteById(TEST_USER_ID);

            // When & Then
            assertThatCode(() -> refreshTokenService.deleteRefreshToken(TEST_USER_ID))
                .doesNotThrowAnyException();
            
            verify(repository).deleteById(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("토큰 저장 후 리프레시 성공 시나리오")
        void saveAndRefresh_FullScenario_Success() {
            // Given - 토큰 저장
            RefreshToken savedToken = spy(new RefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN));
            given(repository.findById(TEST_USER_ID)).willReturn(Optional.empty())
                .willReturn(Optional.of(savedToken))
                .willReturn(Optional.of(savedToken));
            
            // JWT 토큰 정보 설정
            com.example.live_backend.domain.auth.jwt.TokenInfo tokenInfo = 
                com.example.live_backend.domain.auth.jwt.TokenInfo.builder()
                    .valid(true)
                    .expired(false)
                    .category("refresh_token")
                    .userId(TEST_USER_ID)
                    .oauthId(TEST_OAUTH_ID)
                    .role(TEST_ROLE)
                    .build();
            
            // 분리 후: JwtTokenValidator의 validateRefreshToken 메서드 Mock
            given(jwtTokenValidator.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(tokenInfo);
            
            AuthToken newTokens = new AuthToken(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);
            given(generator.generate(TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE)).willReturn(newTokens);

            // When - 토큰 저장
            refreshTokenService.saveRefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN);
            
            // When - 토큰 리프레시
            AuthToken result = refreshTokenService.refresh(TEST_REFRESH_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            
            verify(repository).save(any(RefreshToken.class));
            verify(jwtTokenValidator).validateRefreshToken(TEST_REFRESH_TOKEN);
            verify(generator).generate(TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE);
        }
    }
} 