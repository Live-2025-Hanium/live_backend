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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 테스트")
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_ROLE = "USER";

    @BeforeEach
    void setUp() {
        // 테스트 전에 SecurityContext 클리어한다.
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("인증 정보 조회 기능")
    class GetAuthenticationTests {

        @Test
        @DisplayName("인증된 사용자의 Authentication 객체 조회 - 성공")
        void getAuthentication_AuthenticatedUser_Success() {
            // Given
            Authentication expectedAuth = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, 
                null, 
                Collections.singleton(new SimpleGrantedAuthority(TEST_ROLE))
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(expectedAuth);
            SecurityContextHolder.setContext(securityContext);

            // When
            Authentication result = authenticationService.getAuthentication();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(expectedAuth);
            assertThat(result.getPrincipal()).isEqualTo(TEST_USER_ID);
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

        @Test
        @DisplayName("SecurityContext가 없는 경우 - 빈 컨텍스트로 처리")
        void getAuthentication_NoSecurityContext_ReturnsNull() {
            // Given
            SecurityContextHolder.clearContext(); // null 대신 clearContext 사용

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
            Authentication auth = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, 
                null, 
                Collections.singleton(new SimpleGrantedAuthority(TEST_ROLE))
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
            Authentication auth = new UsernamePasswordAuthenticationToken(
                differentUserId, 
                null, 
                Collections.singleton(new SimpleGrantedAuthority(TEST_ROLE))
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
        @DisplayName("인증되지 않은 사용자의 ID 추출 - 예외 발생")
        void getUserId_UnauthenticatedUser_ThrowsException() {
            // Given
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThatThrownBy(() -> authenticationService.getUserId())
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Principal이 Long 타입이 아닌 경우 - ClassCastException 발생")
        void getUserId_PrincipalNotLong_ThrowsClassCastException() {
            // Given
            String stringPrincipal = "not-a-long";
            Authentication auth = new UsernamePasswordAuthenticationToken(
                stringPrincipal, 
                null, 
                Collections.singleton(new SimpleGrantedAuthority(TEST_ROLE))
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When & Then
            assertThatThrownBy(() -> authenticationService.getUserId())
                .isInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    @DisplayName("다양한 시나리오 테스트")
    class VariousScenarioTests {

        @Test
        @DisplayName("여러 역할을 가진 사용자의 인증 정보 조회")
        void getAuthentication_UserWithMultipleRoles_Success() {
            // Given
            Authentication auth = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, 
                null, 
                java.util.Arrays.asList(
                    new SimpleGrantedAuthority("USER"),
                    new SimpleGrantedAuthority("ADMIN")
                )
            );
            
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            // When
            Authentication result = authenticationService.getAuthentication();
            Long userId = authenticationService.getUserId();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAuthorities()).hasSize(2);
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("SecurityContext 변경 후 올바른 사용자 정보 반환")
        void getAuthentication_AfterContextChange_ReturnsCorrectUser() {
            // Given - 첫 번째 사용자
            Long firstUserId = 100L;
            Authentication firstAuth = new UsernamePasswordAuthenticationToken(
                firstUserId, null, Collections.singleton(new SimpleGrantedAuthority("USER"))
            );
            
            SecurityContext firstContext = mock(SecurityContext.class);
            when(firstContext.getAuthentication()).thenReturn(firstAuth);
            SecurityContextHolder.setContext(firstContext);

            // When - 첫 번째 사용자 ID 확인
            Long firstResult = authenticationService.getUserId();

            // Given - 두 번째 사용자로 변경
            Long secondUserId = 200L;
            Authentication secondAuth = new UsernamePasswordAuthenticationToken(
                secondUserId, null, Collections.singleton(new SimpleGrantedAuthority("ADMIN"))
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
            Authentication auth = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, 
                null, 
                Collections.singleton(new SimpleGrantedAuthority(TEST_ROLE))
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
    }
} 