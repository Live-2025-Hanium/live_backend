package com.example.live_backend.domain.memeber.util;

import com.example.live_backend.domain.example.entity.User;
import com.example.live_backend.domain.example.repository.UserJPARepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @deprecated AuthenticationService를 사용하세요.
 * 이 클래스는 향후 버전에서 제거될 예정입니다.
 */
@Deprecated
@Component
@RequiredArgsConstructor
@Slf4j
public class UserUtil {

	private final UserJPARepository userRepository;
	private final SecurityUtil securityUtil;

	/**
	 * Mock 사용자 데이터가 없는 경우에만 생성합니다.
	 * (이미 존재하는 경우 건너뜁니다.)
	 * @deprecated 이 메서드는 제거될 예정입니다.
	 */
	@Deprecated
	private void ensureMockUsersExist() {
		long userCount = userRepository.count();
		if (userCount == 0) {
			log.info("Mock 사용자 데이터가 없어 생성합니다.");

			// Mock 사용자 생성
			List<User> mockUsers = List.of(
				User.builder()
					.email("test1@example.com")
					.socialProvider(User.SocialProvider.KAKAO)
					.socialId("test-oauth-1")
					.nickname("테스트사용자1")
					.gender(User.Gender.OTHER)
					.birthdate(java.time.LocalDate.of(1990, 1, 1))
					.role(User.Role.MEMBER)
					.cloverBalance(100)
					.deletedAt(null)
					.build(),
				User.builder()
					.email("test2@example.com")
					.socialProvider(User.SocialProvider.GOOGLE)
					.socialId("test-oauth-2")
					.nickname("테스트사용자2")
					.gender(User.Gender.OTHER)
					.birthdate(java.time.LocalDate.of(1990, 1, 1))
					.role(User.Role.MEMBER)
					.cloverBalance(100)
					.deletedAt(null)
					.build(),
				User.builder()
					.email("admin@example.com")
					.socialProvider(User.SocialProvider.GOOGLE)
					.socialId("admin-oauth")
					.nickname("관리자")
					.gender(User.Gender.OTHER)
					.birthdate(java.time.LocalDate.of(1985, 1, 1))
					.role(User.Role.ADMIN)
					.cloverBalance(500)
					.deletedAt(null)
					.build()
			);

			userRepository.saveAll(mockUsers);
			log.info("Mock 사용자 {}명이 생성되었습니다.", mockUsers.size());
		}
	}

	/**
	 * 현재 인증된 사용자 정보를 반환합니다.
	 * @deprecated AuthenticationService를 사용하세요.
	 */
	@Deprecated
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
	 * 현재 인증된 사용자의 ID를 반환합니다.
	 * @deprecated AuthenticationService.getUserId()를 사용하세요.
	 */
	@Deprecated
	public Long getCurrentUserId() {
		return securityUtil.getCurrentUserId();
	}

	/**
	 * 현재 사용자가 관리자 권한을 가지고 있는지 확인합니다.
	 * @deprecated AuthenticationService를 사용하세요.
	 */
	@Deprecated
	public boolean isCurrentUserAdmin() {
		return securityUtil.hasRole("ADMIN");
	}

	/**
	 * 특정 사용자 ID가 현재 인증된 사용자와 같은지 확인합니다.
	 * @deprecated AuthenticationService를 사용하세요.
	 */
	@Deprecated
	public void validateUserAccess(Long userId) {
		Long currentUserId = getCurrentUserId();

		if (!currentUserId.equals(userId) && !isCurrentUserAdmin()) {
			log.warn("권한 없는 사용자 데이터 접근 시도 - 현재 사용자: {}, 요청된 사용자: {}", currentUserId, userId);
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}
	}
}