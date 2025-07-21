package com.example.live_backend.domain.memeber.util;

import com.example.live_backend.domain.example.entity.User;
import com.example.live_backend.domain.example.repository.UserJPARepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserUtil {

    private final SecurityUtil securityUtil;
    private final UserJPARepository userRepository;

    /**
     * InMemoryUserDetailsManager와 연동된 Mock 사용자 DB 데이터 생성
     * SecurityConfig에서 정의한 사용자 ID(1L, 2L)와 매칭
     */
    private void ensureMockUsersExist() {
        // Mock User (ID: 1L) - ROLE_USER
        if (!userRepository.existsById(1L)) {
            log.info("Mock 일반 사용자(ID: 1L)를 생성합니다.");
            
            User mockUser = User.builder()
                    .email("mockuser@example.com")
                    .socialProvider(User.SocialProvider.KAKAO)
                    .socialId("mock-user-1")
                    .nickname("Mock사용자")
                    .gender(User.Gender.OTHER)
                    .birthdate(LocalDate.of(1990, 1, 1))
                    .role(User.Role.MEMBER)
                    .profileImage("https://example.com/mock-user.jpg")
                    .cloverBalance(100)
                    .deletedAt(null)
                    .build();

            userRepository.save(mockUser);
        }

        // Mock Admin (ID: 2L) - ROLE_ADMIN  
        if (!userRepository.existsById(2L)) {
            log.info("Mock 관리자 사용자(ID: 2L)를 생성합니다.");
            
            User mockAdmin = User.builder()
                    .email("mockadmin@example.com")
                    .socialProvider(User.SocialProvider.GOOGLE)
                    .socialId("mock-admin-2")
                    .nickname("Mock관리자")
                    .gender(User.Gender.OTHER)
                    .birthdate(LocalDate.of(1985, 1, 1))
                    .role(User.Role.ADMIN)
                    .profileImage("https://example.com/mock-admin.jpg")
                    .cloverBalance(500)
                    .deletedAt(null)
                    .build();

            userRepository.save(mockAdmin);
        }
    }

    /**
     * 현재 Spring Security 인증된 사용자 정보를 조회
     * InMemoryUserDetailsManager와 연동된 Mock 사용자 반환
     */
    public User getCurrentUser() {
        ensureMockUsersExist();  // Mock 사용자 DB 데이터 확인/생성

        Long currentUserId = securityUtil.getCurrentUserId();
        
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> {
                    log.error("현재 인증된 사용자를 찾을 수 없습니다. userId: {}", currentUserId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 현재 인증된 사용자의 ID를 반환
     */
    public Long getCurrentUserId() {
        return securityUtil.getCurrentUserId();
    }

    /**
     * 현재 사용자가 관리자 권한을 가지고 있는지 확인
     */
    public boolean isCurrentUserAdmin() {
        return securityUtil.hasRole("ADMIN");
    }

    /**
     * 특정 사용자 ID가 현재 인증된 사용자와 같은지 확인
     */
    public void validateUserAccess(Long userId) {
        Long currentUserId = getCurrentUserId();

        if (!currentUserId.equals(userId) && !isCurrentUserAdmin()) {
            log.warn("권한 없는 사용자 데이터 접근 시도 - 현재 사용자: {}, 요청된 사용자: {}", currentUserId, userId);
            throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
    }
} 