package com.example.live_backend.domain.memeber.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.live_backend.domain.auth.service.AuthenticationService;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.response.AuthUserDto;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 카카오 로그인 테스트")
class MemberServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private AuthenticationService authenticationService;

	@Nested
	@DisplayName("카카오 로그인/회원가입")
	class KakaoLoginOrRegister {

		@Test
		@DisplayName("신규 회원 - OAuth 정보로 회원가입하고 isNewUser=true 반환")
		void shouldRegisterNewMemberWhenOauthIdNotExists() {

			KakaoLoginRequestDto request = createKakaoLoginRequest();
			given(memberRepository.findByOauthId(request.getOauthId()))
				.willReturn(Optional.empty());
			
			Member savedMember = createMemberFromRequest(request, 1L);
			given(memberRepository.save(any(Member.class)))
				.willReturn(savedMember);

			AuthUserDto result = memberService.loginOrRegister(request);

			assertThat(result.isNewUser()).isTrue();
			assertThat(result.getOauthId()).isEqualTo(request.getOauthId());
			assertThat(result.getEmail()).isEqualTo(request.getEmail());
			assertThat(result.getNickname()).isEqualTo(request.getNickname());
			assertThat(result.getRole()).isEqualTo(Role.USER);
			
			verify(memberRepository).save(any(Member.class));
		}

		@Test
		@DisplayName("기존 회원 - 프로필 업데이트 없이 로그인하고 isNewUser=false 반환")
		void shouldLoginExistingMemberWithoutProfileUpdate() {

			KakaoLoginRequestDto request = createKakaoLoginRequest();
			Member existingMember = createExistingMember();
			String originalNickname = existingMember.getProfile().getNickname();
			
					given(memberRepository.findByOauthId(request.getOauthId()))
			.willReturn(Optional.of(existingMember));

			AuthUserDto result = memberService.loginOrRegister(request);

			assertThat(result.isNewUser()).isFalse();
			assertThat(result.getNickname()).isEqualTo(originalNickname); // 프로필 업데이트 안됨
			assertThat(existingMember.getProfile().getNickname()).isEqualTo(originalNickname); // 원본도 변경 안됨
			
			verify(memberRepository, never()).save(any(Member.class)); // 저장하지 않음
		}

		private KakaoLoginRequestDto createKakaoLoginRequest() {
			KakaoLoginRequestDto request = new KakaoLoginRequestDto();

			setField(request, "oauthId", "12345");
			setField(request, "email", "test@example.com");
			setField(request, "nickname", "카카오닉네임");
			setField(request, "profileImageUrl", "https://example.com/profile.jpg");
			return request;
		}

		private Member createMemberFromRequest(KakaoLoginRequestDto request, Long id) {
			Profile profile = Profile.builder()
				.nickname(request.getNickname())
				.profileImageUrl(request.getProfileImageUrl())
				.build();

					Member member = Member.builder()
			.oauthId(request.getOauthId())
			.email(request.getEmail())
			.role(Role.USER)
			.profile(profile)
			.build();
			
			setField(member, "id", id);
			return member;
		}

		private Member createExistingMember() {
			Profile profile = Profile.builder()
				.nickname("기존닉네임")
				.profileImageUrl("https://example.com/old.jpg")
				.build();

					Member member = Member.builder()
			.oauthId("12345")
			.email("existing@example.com")
			.role(Role.USER)
			.profile(profile)
				.build();
			
			setField(member, "id", 1L);
			return member;
		}
	}

	@Nested
	@DisplayName("닉네임 중복 확인")
	class NicknameCheck {

		@Test
		@DisplayName("유효하지 않은 닉네임 - Profile VO 검증 에러 반환")
		void shouldReturnValidationErrorForInvalidNickname() {

			String invalidNickname = "";

			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(invalidNickname);

			assertThat(result.isAvailable()).isFalse();
			assertThat(result.getMessage()).isEqualTo(ErrorCode.MEMBER_NICKNAME_REQUIRED.getDetail());
		}

		@Test
		@DisplayName("로그인된 사용자 - 자신의 현재 닉네임은 사용 가능")
		void shouldAllowCurrentUserOwnNickname() {

			String nickname = "내닉네임";
			Long currentUserId = 1L;
			
			given(authenticationService.getUserId()).willReturn(currentUserId);
			given(memberRepository.existsByProfileNicknameAndIdNot(nickname, currentUserId))
				.willReturn(false);

			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(nickname);

			assertThat(result.isAvailable()).isTrue();
			assertThat(result.getMessage()).contains("사용 가능한");
		}

		@Test
		@DisplayName("로그인된 사용자 - 다른 사용자가 사용 중인 닉네임은 불가능")
		void shouldRejectNicknameUsedByOtherUser() {
			// given
			String nickname = "다른사람닉네임";
			Long currentUserId = 1L;
			
			given(authenticationService.getUserId()).willReturn(currentUserId);
			given(memberRepository.existsByProfileNicknameAndIdNot(nickname, currentUserId))
				.willReturn(true);

			// when
			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(nickname);

			// then
			assertThat(result.isAvailable()).isFalse();
			assertThat(result.getMessage()).contains("이미 사용 중인");
		}

		@Test
		@DisplayName("비로그인 사용자 - 전체 닉네임 중복 확인")
		void shouldCheckAllNicknamesForAnonymousUser() {
			// given
			String nickname = "사용가능닉네임";
			
			given(authenticationService.getUserId())
				.willThrow(new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER));
			given(memberRepository.existsByProfileNickname(nickname))
				.willReturn(false);

			// when
			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(nickname);

			// then
			assertThat(result.isAvailable()).isTrue();
			verify(memberRepository).existsByProfileNickname(nickname); // 전체 검색
			verify(memberRepository, never()).existsByProfileNicknameAndIdNot(any(), any()); // 특정 사용자 제외 검색 안함
		}
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			var field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {

			try {
				var field = target.getClass().getSuperclass().getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(target, value);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to set field: " + fieldName, ex);
			}
		}
	}
} 