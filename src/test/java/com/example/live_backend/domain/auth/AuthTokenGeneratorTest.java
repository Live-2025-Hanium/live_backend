package com.example.live_backend.domain.auth;

import com.example.live_backend.domain.auth.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 토큰 생성기 테스트")
class AuthTokenGeneratorTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthTokenGenerator authTokenGenerator;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_OAUTH_ID = "oauth123";
    private final String TEST_ROLE = "USER";
    private final String EXPECTED_ACCESS_TOKEN = "generated.access.token";
    private final String EXPECTED_REFRESH_TOKEN = "generated.refresh.token";
    private final Long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L; // 1시간
    private final Long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7일

    @BeforeEach
    void setUp() {
        // 리플렉션을 사용하여 private 필드 설정
        ReflectionTestUtils.setField(authTokenGenerator, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(authTokenGenerator, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
    }

    @Nested
    @DisplayName("토큰 생성 기능")
    class GenerateTokenTests {

        @Test
        @DisplayName("정상적인 토큰 쌍 생성 - 성공")
        void generate_ValidInput_Success() {
            // Given
            given(jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_ACCESS_TOKEN);
            given(jwtUtil.createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, REFRESH_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_REFRESH_TOKEN);

            // When
            AuthToken result = authTokenGenerator.generate(TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);

            verify(jwtUtil).createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_EXPIRATION);
            verify(jwtUtil).createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, REFRESH_TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("다른 사용자 정보로 토큰 생성 - 각각 다른 토큰 반환")
        void generate_DifferentUsers_ReturnDifferentTokens() {
            // Given
            Long userId1 = 100L;
            Long userId2 = 200L;
            String oauthId1 = "oauth100";
            String oauthId2 = "oauth200";
            String role1 = "USER";
            String role2 = "ADMIN";
            
            String accessToken1 = "access.token.1";
            String refreshToken1 = "refresh.token.1";
            String accessToken2 = "access.token.2";
            String refreshToken2 = "refresh.token.2";

            given(jwtUtil.createJwt("access_token", userId1, oauthId1, role1, ACCESS_TOKEN_EXPIRATION))
                .willReturn(accessToken1);
            given(jwtUtil.createJwt("refresh_token", userId1, oauthId1, role1, REFRESH_TOKEN_EXPIRATION))
                .willReturn(refreshToken1);
            given(jwtUtil.createJwt("access_token", userId2, oauthId2, role2, ACCESS_TOKEN_EXPIRATION))
                .willReturn(accessToken2);
            given(jwtUtil.createJwt("refresh_token", userId2, oauthId2, role2, REFRESH_TOKEN_EXPIRATION))
                .willReturn(refreshToken2);

            // When
            AuthToken result1 = authTokenGenerator.generate(userId1, oauthId1, role1);
            AuthToken result2 = authTokenGenerator.generate(userId2, oauthId2, role2);

            // Then
            assertThat(result1.accessToken()).isEqualTo(accessToken1);
            assertThat(result1.refreshToken()).isEqualTo(refreshToken1);
            assertThat(result2.accessToken()).isEqualTo(accessToken2);
            assertThat(result2.refreshToken()).isEqualTo(refreshToken2);
            
            // 토큰들이 서로 다른지 확인
            assertThat(result1.accessToken()).isNotEqualTo(result2.accessToken());
            assertThat(result1.refreshToken()).isNotEqualTo(result2.refreshToken());
        }

        @Test
        @DisplayName("null 사용자 ID로 토큰 생성 - 정상 처리")
        void generate_NullUserId_Success() {
            // Given
            Long nullUserId = null;
            given(jwtUtil.createJwt("access_token", nullUserId, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_ACCESS_TOKEN);
            given(jwtUtil.createJwt("refresh_token", nullUserId, TEST_OAUTH_ID, TEST_ROLE, REFRESH_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_REFRESH_TOKEN);

            // When
            AuthToken result = authTokenGenerator.generate(nullUserId, TEST_OAUTH_ID, TEST_ROLE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);

            verify(jwtUtil).createJwt("access_token", nullUserId, TEST_OAUTH_ID, TEST_ROLE, ACCESS_TOKEN_EXPIRATION);
            verify(jwtUtil).createJwt("refresh_token", nullUserId, TEST_OAUTH_ID, TEST_ROLE, REFRESH_TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("빈 문자열 OAuth ID로 토큰 생성 - 정상 처리")
        void generate_EmptyOauthId_Success() {
            // Given
            String emptyOauthId = "";
            given(jwtUtil.createJwt("access_token", TEST_USER_ID, emptyOauthId, TEST_ROLE, ACCESS_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_ACCESS_TOKEN);
            given(jwtUtil.createJwt("refresh_token", TEST_USER_ID, emptyOauthId, TEST_ROLE, REFRESH_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_REFRESH_TOKEN);

            // When
            AuthToken result = authTokenGenerator.generate(TEST_USER_ID, emptyOauthId, TEST_ROLE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);

            verify(jwtUtil).createJwt("access_token", TEST_USER_ID, emptyOauthId, TEST_ROLE, ACCESS_TOKEN_EXPIRATION);
            verify(jwtUtil).createJwt("refresh_token", TEST_USER_ID, emptyOauthId, TEST_ROLE, REFRESH_TOKEN_EXPIRATION);
        }
    }

    @Nested
    @DisplayName("만료 시간 설정 테스트")
    class ExpirationTimeTests {

        @Test
        @DisplayName("다른 만료 시간 설정 시 올바른 값으로 토큰 생성")
        void generate_DifferentExpirationTimes_UsesCorrectValues() {
            // Given
            Long customAccessExpiration = 30 * 60 * 1000L; // 30분
            Long customRefreshExpiration = 14 * 24 * 60 * 60 * 1000L; // 14일
            
            ReflectionTestUtils.setField(authTokenGenerator, "accessTokenExpiration", customAccessExpiration);
            ReflectionTestUtils.setField(authTokenGenerator, "refreshTokenExpiration", customRefreshExpiration);

            given(jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, customAccessExpiration))
                .willReturn(EXPECTED_ACCESS_TOKEN);
            given(jwtUtil.createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, customRefreshExpiration))
                .willReturn(EXPECTED_REFRESH_TOKEN);

            // When
            AuthToken result = authTokenGenerator.generate(TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);

            verify(jwtUtil).createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, customAccessExpiration);
            verify(jwtUtil).createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, TEST_ROLE, customRefreshExpiration);
        }
    }

    @Nested
    @DisplayName("다양한 역할 테스트")
    class DifferentRoleTests {

        @Test
        @DisplayName("ADMIN 역할로 토큰 생성 - 성공")
        void generate_AdminRole_Success() {
            // Given
            String adminRole = "ADMIN";
            given(jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, adminRole, ACCESS_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_ACCESS_TOKEN);
            given(jwtUtil.createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, adminRole, REFRESH_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_REFRESH_TOKEN);

            // When
            AuthToken result = authTokenGenerator.generate(TEST_USER_ID, TEST_OAUTH_ID, adminRole);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);

            verify(jwtUtil).createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, adminRole, ACCESS_TOKEN_EXPIRATION);
            verify(jwtUtil).createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, adminRole, REFRESH_TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("GUEST 역할로 토큰 생성 - 성공")
        void generate_GuestRole_Success() {
            // Given
            String guestRole = "GUEST";
            given(jwtUtil.createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, guestRole, ACCESS_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_ACCESS_TOKEN);
            given(jwtUtil.createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, guestRole, REFRESH_TOKEN_EXPIRATION))
                .willReturn(EXPECTED_REFRESH_TOKEN);

            // When
            AuthToken result = authTokenGenerator.generate(TEST_USER_ID, TEST_OAUTH_ID, guestRole);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);

            verify(jwtUtil).createJwt("access_token", TEST_USER_ID, TEST_OAUTH_ID, guestRole, ACCESS_TOKEN_EXPIRATION);
            verify(jwtUtil).createJwt("refresh_token", TEST_USER_ID, TEST_OAUTH_ID, guestRole, REFRESH_TOKEN_EXPIRATION);
        }
    }
} 