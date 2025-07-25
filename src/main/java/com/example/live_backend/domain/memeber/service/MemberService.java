package com.example.live_backend.domain.memeber.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.live_backend.domain.auth.service.AuthenticationService;
import com.example.live_backend.domain.auth.dto.response.AuthUserDto;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.Occupation;
import com.example.live_backend.domain.memeber.entity.vo.BirthDate;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final AuthenticationService authService;

	@Transactional
	public AuthUserDto loginOrRegister(KakaoLoginRequestDto request) {
		return memberRepository.findByOauthId(request.getOauthId())
			.map(existingMember -> {
				// 기존 회원 - 로그인 처리만 수행
				return toAuthUserDto(existingMember, false);
			})
			.orElseGet(() -> {
				// 신규 회원 - 회원가입 처리
				Member newMember = createNewMember(request);
				Member savedMember = memberRepository.save(newMember);
				return toAuthUserDto(savedMember, true);
			});
	}

	public NicknameCheckResponseDto checkNicknameAvailability(String nickname) {

		try {
			Profile.builder()
				.nickname(nickname)
				.profileImageUrl(null)
				.build();
		} catch (CustomException e) {
			return new NicknameCheckResponseDto(false, e.getErrorCode().getDetail());
		}

		try {
			Long currentUserId = authService.getUserId();
			boolean isDuplicate = memberRepository.existsByProfileNicknameAndIdNot(nickname, currentUserId);
			if (isDuplicate) {
				return NicknameCheckResponseDto.unavailable();
			} else {
				return NicknameCheckResponseDto.available();
			}
		} catch (Exception e) {
			boolean isDuplicate = memberRepository.existsByProfileNickname(nickname);
			if (isDuplicate) {
				return NicknameCheckResponseDto.unavailable();
			} else {
				return NicknameCheckResponseDto.available();
			}
		}
	}

	private Member createNewMember(KakaoLoginRequestDto request) {
		Profile profile = Profile.builder()
			.nickname(request.getNickname())
			.profileImageUrl(request.getProfileImageUrl())
			.build();

		return Member.builder()
			.oauthId(request.getOauthId())
			.email(request.getEmail())
			.role(Role.USER)
			.profile(profile)
			.build();
	}

	private AuthUserDto toAuthUserDto(Member member, boolean isNewUser) {
		return AuthUserDto.builder()
			.id(member.getId())
			.oauthId(member.getOauthId())
			.email(member.getEmail())
			.nickname(member.getProfile().getNickname())
			.profileImageUrl(member.getProfile().getProfileImageUrl())
			.role(member.getRole())
			.isNewUser(isNewUser)
			.build();
	}

	@Transactional
	public MemberResponseDto registerOrUpdateProfile(MemberProfileRequestDto dto) {
		Long userId = authService.getUserId();

		Member member = saveOrUpdateMemberProfile(userId, dto);
		return toDto(member);
	}

	private Member saveOrUpdateMemberProfile(Long userId, MemberProfileRequestDto dto) {
		Profile profile = buildProfile(dto);
		Gender gender = Gender.valueOf(dto.getGender());
		BirthDate birthDate = BirthDate.of(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
		Occupation occupation = Occupation.valueOf(dto.getOccupation());
		String detail = dto.getOccupationDetail();

		return memberRepository.findById(userId)
			.map(existing -> {
				existing.updateProfile(profile);
				existing.updateDetails(gender, birthDate, occupation, detail);
				return existing;
			})
			.orElseGet(() ->
				memberRepository.save(
					Member.builder()
						.profile(profile)
						.gender(gender)
						.birthDate(birthDate)
						.occupation(occupation)
						.occupationDetail(detail)
						.build()
				)
			);
	}

	private Profile buildProfile(MemberProfileRequestDto dto) {
		return Profile.builder()
			.nickname(dto.getNickname())
			.profileImageUrl(dto.getProfileImageUrl())
			.build();
	}

	private MemberResponseDto toDto(Member member) {
		return new MemberResponseDto(
			member.getId(),
			member.getProfile().getNickname(),
			member.getProfile().getProfileImageUrl(),
			member.getGender().name(),
			member.getBirthDate().getValue().toString(),
			member.getOccupation().name(),
			member.getOccupationDetail()
		);
	}
}