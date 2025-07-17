package com.example.live_backend.domain.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT 유틸리티 테스트")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET_KEY = "testSecretKeyForJwtTokenGenerationAndValidation1234567890";
    private final Long TEST_USER_ID = 123L;
    private final String TEST_OAUTH_ID = "oauth123";
    private final String TEST_ROLE = "USER";
    private final Long ACCESS_TOKEN_TTL = 60 * 60 * 1000L; // 1시간
    private final Long REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L; // 7일

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY);
    }

    @Nested
    @DisplayName("JWT 토큰 생성 테스트")
    class CreateJwtTests {

        @Test
        @DisplayName("정상적인 액세스 토큰 생성 - 성공")
        void createAccessToken_Success() {
            // When
            String token = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_TTL);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 형태
            assertThat(jwtUtil.getCategory(token)).isEqualTo("access_token");
            assertThat(jwtUtil.getUserId(token)).isEqualTo(TEST_USER_ID);
            assertThat(jwtUtil.getOauthId(token)).isEqualTo(TEST_OAUTH_ID);
            assertThat(jwtUtil.getRole(token)).isEqualTo(TEST_ROLE);
            assertThat(jwtUtil.isExpired(token)).isFalse();
        }

        @Test
        @DisplayName("정상적인 리프레시 토큰 생성 - 성공")
        void createRefreshToken_Success() {
            // When
            String token = jwtUtil.createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, REFRESH_TOKEN_TTL);

            // Then
            assertThat(token).isNotNull();
            assertThat(jwtUtil.getCategory(token)).isEqualTo("refresh_token");
            assertThat(jwtUtil.getUserId(token)).isEqualTo(TEST_USER_ID);
            assertThat(jwtUtil.getOauthId(token)).isEqualTo(TEST_OAUTH_ID);
            assertThat(jwtUtil.getRole(token)).isEqualTo(TEST_ROLE);
            assertThat(jwtUtil.isExpired(token)).isFalse();
        }

        @Test
        @DisplayName("만료 시간이 0인 토큰 생성 - 즉시 만료")
        void createExpiredToken_Success() {
            // When
            String token = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, 0L);

            // Then
            assertThat(token).isNotNull();
            assertThat(jwtUtil.isExpired(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("JWT 토큰 검증 테스트")
    class ValidateJwtTests {

        @Test
        @DisplayName("유효한 토큰 검증 - 성공")
        void validateValidToken_Success() {
            // Given
            String token = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_TTL);

            // When & Then
            assertThat(jwtUtil.isExpired(token)).isFalse();
        }

        @Test
        @DisplayName("만료된 토큰 검증 - 만료됨")
        void validateExpiredToken_IsExpired() {
            // Given
            String token = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, -1000L);

            // When & Then
            assertThat(jwtUtil.isExpired(token)).isTrue();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 - 예외 발생")
        void validateMalformedToken_ThrowsException() {
            // Given
            String malformedToken = "invalid.token.format";

            // When & Then
            assertThatThrownBy(() -> jwtUtil.isExpired(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("빈 토큰 검증 - 예외 발생")
        void validateEmptyToken_ThrowsException() {
            // Given
            String emptyToken = "";

            // When & Then
            assertThatThrownBy(() -> jwtUtil.isExpired(emptyToken))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("JWT 클레임 추출 테스트")
    class ExtractClaimsTests {

        private String validToken;

        @BeforeEach
        void setUpToken() {
            validToken = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_TTL);
        }

        @Test
        @DisplayName("카테고리 추출 - 성공")
        void extractCategory_Success() {
            // When
            String category = jwtUtil.getCategory(validToken);

            // Then
            assertThat(category).isEqualTo("access_token");
        }

        @Test
        @DisplayName("사용자 ID 추출 - 성공")
        void extractUserId_Success() {
            // When
            Long userId = jwtUtil.getUserId(validToken);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("OAuth ID 추출 - 성공")
        void extractOauthId_Success() {
            // When
            String oauthId = jwtUtil.getOauthId(validToken);

            // Then
            assertThat(oauthId).isEqualTo(TEST_OAUTH_ID);
        }

        @Test
        @DisplayName("역할 추출 - 성공")
        void extractRole_Success() {
            // When
            String role = jwtUtil.getRole(validToken);

            // Then
            assertThat(role).isEqualTo(TEST_ROLE);
        }

        @Test
        @DisplayName("만료된 토큰에서 클레임 추출 - 예외 발생")
        void extractFromExpiredToken_ThrowsException() {
            // Given
            String expiredToken = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, -1000L);

            // When & Then
            assertThatThrownBy(() -> jwtUtil.getCategory(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
            assertThatThrownBy(() -> jwtUtil.getUserId(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
            assertThatThrownBy(() -> jwtUtil.getOauthId(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
            assertThatThrownBy(() -> jwtUtil.getRole(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
        }
    }

    @Nested
    @DisplayName("다양한 시나리오 테스트")
    class VariousScenarioTests {

        @Test
        @DisplayName("다른 사용자 정보로 토큰 생성 - 각각 다른 값 반환")
        void createTokensWithDifferentUsers_ReturnDifferentValues() {
            // Given
            Long userId1 = 100L;
            Long userId2 = 200L;
            String oauthId1 = "oauth100";
            String oauthId2 = "oauth200";
            String role1 = "USER";
            String role2 = "ADMIN";

            // When
            String token1 = jwtUtil.createJwt("access_token", userId1, oauthId1, role1, ACCESS_TOKEN_TTL);
            String token2 = jwtUtil.createJwt("access_token", userId2, oauthId2, role2, ACCESS_TOKEN_TTL);

            // Then
            assertThat(jwtUtil.getUserId(token1)).isEqualTo(userId1);
            assertThat(jwtUtil.getUserId(token2)).isEqualTo(userId2);
            assertThat(jwtUtil.getOauthId(token1)).isEqualTo(oauthId1);
            assertThat(jwtUtil.getOauthId(token2)).isEqualTo(oauthId2);
            assertThat(jwtUtil.getRole(token1)).isEqualTo(role1);
            assertThat(jwtUtil.getRole(token2)).isEqualTo(role2);
        }

        @Test
        @DisplayName("서로 다른 카테고리의 토큰 생성 - 각각 다른 카테고리 반환")
        void createTokensWithDifferentCategories_ReturnDifferentCategories() {
            // When
            String accessToken = jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_TTL);
            String refreshToken = jwtUtil.createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, REFRESH_TOKEN_TTL);

            // Then
            assertThat(jwtUtil.getCategory(accessToken)).isEqualTo("access_token");
            assertThat(jwtUtil.getCategory(refreshToken)).isEqualTo("refresh_token");

            assertThat(jwtUtil.getUserId(accessToken)).isEqualTo(jwtUtil.getUserId(refreshToken));
            assertThat(jwtUtil.getOauthId(accessToken)).isEqualTo(jwtUtil.getOauthId(refreshToken));
            assertThat(jwtUtil.getRole(accessToken)).isEqualTo(jwtUtil.getRole(refreshToken));
        }
    }
} 