package com.example.live_backend.domain.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.security.PrincipalDetails;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 테스트")
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_OAUTH_ID = "oauth_123";
    private final String TEST_ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private PrincipalDetails createTestPrincipalDetails(Long userId, String role) {
        return new PrincipalDetails(userId, TEST_OAUTH_ID, role, "테스트사용자", "test@example.com");
    }

    @Nested
    @DisplayName("인증 정보 조회 기능")
    class GetAuthenticationTests {

        @Test
        @DisplayName("인증된 사용자의 Authentication 객체 조회 - 성공")
        void getAuthentication_AuthenticatedUser_Success() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, TEST_ROLE);
            Authentication expectedAuth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(expectedAuth);
            SecurityContextHolder.setContext(securityContext);

            // When
            Authentication result = authenticationService.getAuthentication();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(expectedAuth);
            assertThat(result.getPrincipal()).isInstanceOf(PrincipalDetails.class);
            
            PrincipalDetails principal = (PrincipalDetails) result.getPrincipal();
            assertThat(principal.getMemberId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 Authentication 객체 조회 - null 반환")
        void getAuthentication_UnauthenticatedUser_ReturnsNull() {
            // Given
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When
            Authentication result = authenticationService.getAuthentication();

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("사용자 ID 추출 기능")
    class GetUserIdTests {

        @Test
        @DisplayName("인증된 사용자의 ID 추출 - 성공")
        void getUserId_AuthenticatedUser_Success() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, TEST_ROLE);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When
            Long result = authenticationService.getUserId();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("다른 사용자 ID로 인증된 경우 - 해당 ID 반환")
        void getUserId_DifferentUserId_ReturnsCorrectId() {
            // Given
            Long differentUserId = 999L;
            PrincipalDetails principalDetails = createTestPrincipalDetails(differentUserId, TEST_ROLE);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When
            Long result = authenticationService.getUserId();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(differentUserId);
            assertThat(result).isNotEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 ID 추출 - CustomException 발생")
        void getUserId_UnauthenticatedUser_ThrowsCustomException() {
            // Given
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThatThrownBy(() -> authenticationService.getUserId())
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DENIED_UNAUTHORIZED_USER);
        }

        @Test
        @DisplayName("Principal이 PrincipalDetails가 아닌 경우 - CustomException 발생")
        void getUserId_PrincipalNotPrincipalDetails_ThrowsCustomException() {
            // Given
            String stringPrincipal = "not-a-principal-details";
            Authentication auth = new UsernamePasswordAuthenticationToken(
                stringPrincipal, 
                null, 
                null
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThatThrownBy(() -> authenticationService.getUserId())
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("Principal이 anonymousUser인 경우 - CustomException 발생")
        void getUserId_AnonymousUser_ThrowsCustomException() {
            // Given
            Authentication auth = new UsernamePasswordAuthenticationToken(
                "anonymousUser", 
                null, 
                null
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThatThrownBy(() -> authenticationService.getUserId())
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
    }

    @Nested
    @DisplayName("현재 사용자 정보 조회 기능")
    class GetCurrentUserTests {

        @Test
        @DisplayName("인증된 사용자의 PrincipalDetails 조회 - 성공")
        void getCurrentUser_AuthenticatedUser_Success() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, TEST_ROLE);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When
            PrincipalDetails result = authenticationService.getCurrentUser();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getMemberKey()).isEqualTo(TEST_OAUTH_ID);
            assertThat(result.getRole()).isEqualTo(TEST_ROLE);
        }
    }

    @Nested
    @DisplayName("권한 확인 기능")
    class AuthorityTests {

        @Test
        @DisplayName("사용자 권한 확인 - 성공")
        void hasRole_UserRole_Success() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, "ROLE_USER");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThat(authenticationService.hasRole("USER")).isTrue();
            assertThat(authenticationService.hasRole("ADMIN")).isFalse();
            assertThat(authenticationService.isUser()).isTrue();
            assertThat(authenticationService.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("관리자 권한 확인 - 성공")
        void hasRole_AdminRole_Success() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, "ROLE_ADMIN");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThat(authenticationService.hasRole("ADMIN")).isTrue();
            assertThat(authenticationService.hasRole("USER")).isFalse();
            assertThat(authenticationService.isAdmin()).isTrue();
            assertThat(authenticationService.isUser()).isFalse();
        }

        @Test
        @DisplayName("인증되지 않은 사용자 권한 확인 - false 반환")
        void hasRole_UnauthenticatedUser_ReturnsFalse() {
            // Given
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThat(authenticationService.hasRole("USER")).isFalse();
            assertThat(authenticationService.hasRole("ADMIN")).isFalse();
            assertThat(authenticationService.isUser()).isFalse();
            assertThat(authenticationService.isAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("다양한 시나리오 테스트")
    class VariousScenarioTests {

        @Test
        @DisplayName("SecurityContext 변경 후 올바른 사용자 정보 반환")
        void getAuthentication_AfterContextChange_ReturnsCorrectUser() {
            // Given - 첫 번째 사용자
            Long firstUserId = 100L;
            PrincipalDetails firstPrincipal = createTestPrincipalDetails(firstUserId, "ROLE_USER");
            Authentication firstAuth = new UsernamePasswordAuthenticationToken(
                firstPrincipal, null, firstPrincipal.getAuthorities()
            );
            
            SecurityContext firstContext = mock(SecurityContext.class);
            when(firstContext.getAuthentication()).thenReturn(firstAuth);
            SecurityContextHolder.setContext(firstContext);

            // When - 첫 번째 사용자 ID 확인
            Long firstResult = authenticationService.getUserId();

            // Given - 두 번째 사용자로 변경
            Long secondUserId = 200L;
            PrincipalDetails secondPrincipal = createTestPrincipalDetails(secondUserId, "ROLE_ADMIN");
            Authentication secondAuth = new UsernamePasswordAuthenticationToken(
                secondPrincipal, null, secondPrincipal.getAuthorities()
            );
            
            SecurityContext secondContext = mock(SecurityContext.class);
            when(secondContext.getAuthentication()).thenReturn(secondAuth);
            SecurityContextHolder.setContext(secondContext);

            // When - 두 번째 사용자 ID 확인
            Long secondResult = authenticationService.getUserId();

            // Then
            assertThat(firstResult).isEqualTo(firstUserId);
            assertThat(secondResult).isEqualTo(secondUserId);
            assertThat(firstResult).isNotEqualTo(secondResult);
        }

        @Test
        @DisplayName("동일한 사용자로 여러 번 호출 - 일관된 결과")
        void getUserId_SameUserMultipleCalls_ConsistentResult() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, TEST_ROLE);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When
            Long result1 = authenticationService.getUserId();
            Long result2 = authenticationService.getUserId();
            Long result3 = authenticationService.getUserId();

            // Then
            assertThat(result1).isEqualTo(TEST_USER_ID);
            assertThat(result2).isEqualTo(TEST_USER_ID);
            assertThat(result3).isEqualTo(TEST_USER_ID);
            assertThat(result1).isEqualTo(result2).isEqualTo(result3);
        }

        @Test
        @DisplayName("인증 상태 확인 - 인증된 사용자")
        void isAuthenticated_AuthenticatedUser_ReturnsTrue() {
            // Given
            PrincipalDetails principalDetails = createTestPrincipalDetails(TEST_USER_ID, TEST_ROLE);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                principalDetails, 
                null, 
                principalDetails.getAuthorities()
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThat(authenticationService.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("인증 상태 확인 - 인증되지 않은 사용자")
        void isAuthenticated_UnauthenticatedUser_ReturnsFalse() {
            // Given
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThat(authenticationService.isAuthenticated()).isFalse();
        }
    }
} 