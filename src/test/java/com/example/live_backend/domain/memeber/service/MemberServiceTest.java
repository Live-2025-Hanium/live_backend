package com.example.live_backend.domain.memeber.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.live_backend.domain.auth.dto.response.AuthUserDto;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 카카오 로그인 테스트")
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

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
	}

	@Nested
	@DisplayName("닉네임 중복 검사")
	class NicknameAvailabilityCheck {

		@Test
		@DisplayName("로그인 사용자 - 자신의 현재 닉네임은 사용 가능")
		void shouldAllowCurrentUserNickname() {
			// Given
			String nickname = "현재닉네임";
			Long currentUserId = 1L;
			given(memberRepository.existsByProfileNicknameAndIdNot(nickname, currentUserId))
				.willReturn(false);

			// When
			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(nickname, currentUserId);

			// Then
			assertThat(result.isAvailable()).isTrue();
			verify(memberRepository).existsByProfileNicknameAndIdNot(nickname, currentUserId);
		}

		@Test
		@DisplayName("로그인 사용자 - 다른 사용자가 사용 중인 닉네임은 사용 불가능")
		void shouldRejectDuplicateNicknameForLoggedInUser() {
			// Given
			String nickname = "중복닉네임";
			Long currentUserId = 1L;
			given(memberRepository.existsByProfileNicknameAndIdNot(nickname, currentUserId))
				.willReturn(true);

			// When
			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(nickname, currentUserId);

			// Then
			assertThat(result.isAvailable()).isFalse();
			verify(memberRepository).existsByProfileNicknameAndIdNot(nickname, currentUserId);
		}

		@Test
		@DisplayName("비로그인 사용자 - 전체 중복 확인")
		void shouldCheckGlobalDuplicateForAnonymousUser() {
			// Given
			String nickname = "닉네임";
			given(memberRepository.existsByProfileNickname(nickname))
				.willReturn(false);

			// When
			NicknameCheckResponseDto result = memberService.checkNicknameAvailability(nickname, null);

			// Then
			assertThat(result.isAvailable()).isTrue();
			verify(memberRepository).existsByProfileNickname(nickname);
		}
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
		
		setField(member, "id", 2L);
		return member;
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

	@Nested
	@DisplayName("온보딩 프로필 등록/수정")
	class OnboardingProfileTest {

		@Test
		@DisplayName("성공 - 기존 회원 프로필 업데이트")
		void shouldUpdateExistingMemberProfile() {
			// Given
			Long userId = 1L;
			MemberProfileRequestDto dto = createValidMemberProfileRequest();
			Member existingMember = createMemberWithProfile(userId);
			
			given(memberRepository.findById(userId)).willReturn(Optional.of(existingMember));

			// When
			MemberResponseDto result = memberService.registerOrUpdateProfile(dto, userId);

			// Then
			assertThat(result.getNickname()).isEqualTo("새로운닉네임123");
			assertThat(result.getGender()).isEqualTo("MALE");
			assertThat(result.getOccupation()).isEqualTo("STUDENT");
			verify(memberRepository).findById(userId);
		}

		@Test
		@DisplayName("성공 - 신규 회원 생성")
		void shouldCreateNewMemberWhenUserNotFound() {
			// Given
			Long userId = 999L;
			MemberProfileRequestDto dto = createValidMemberProfileRequest();
			
			given(memberRepository.findById(userId)).willReturn(Optional.empty());
			given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
				Member member = invocation.getArgument(0);
				setField(member, "id", userId);
				return member;
			});

			// When
			MemberResponseDto result = memberService.registerOrUpdateProfile(dto, userId);

			// Then
			assertThat(result.getNickname()).isEqualTo("새로운닉네임123");
			assertThat(result.getGender()).isEqualTo("MALE");
			assertThat(result.getOccupation()).isEqualTo("STUDENT");
			verify(memberRepository).findById(userId);
			verify(memberRepository).save(any(Member.class));
		}

		private MemberProfileRequestDto createValidMemberProfileRequest() {
			MemberProfileRequestDto dto = new MemberProfileRequestDto();
			setField(dto, "nickname", "새로운닉네임123");
			setField(dto, "profileImageUrl", "https://s3.amazonaws.com/profile/image.jpg");
			setField(dto, "gender", "MALE");
			setField(dto, "birthYear", 1995);
			setField(dto, "birthMonth", 3);
			setField(dto, "birthDay", 15);
			setField(dto, "occupation", "STUDENT");
			setField(dto, "occupationDetail", null);
			return dto;
		}



		private Member createMemberWithProfile(Long userId) {
			Profile profile = Profile.builder()
				.nickname("기존닉네임")
				.profileImageUrl("existing-url")
				.build();
			
			Member member = Member.builder()
				.oauthId("oauth-" + userId)
				.email("test" + userId + "@example.com")
				.role(Role.USER)
				.profile(profile)
				.build();
			setField(member, "id", userId);
			return member;
		}
	}
} 