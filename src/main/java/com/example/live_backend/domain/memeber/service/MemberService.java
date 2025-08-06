package com.example.live_backend.domain.memeber.service;


import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public NicknameCheckResponseDto checkNicknameAvailability(String nickname, Long currentUserId) {
		try {
			Profile.builder()
				.nickname(nickname)
				.profileImageUrl(null)
				.build();
		} catch (CustomException e) {
			return new NicknameCheckResponseDto(false, e.getErrorCode().getDetail());
		}

		if (currentUserId != null) {
			// 로그인된 사용자: 자신의 현재 닉네임은 제외하고 중복 확인
			boolean isDuplicate = memberRepository.existsByProfileNicknameAndIdNot(nickname, currentUserId);
			if (isDuplicate) {
				return NicknameCheckResponseDto.unavailable();
			} else {
				return NicknameCheckResponseDto.available();
			}
		} else {
			// 비로그인 사용자: 전체 중복 확인
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
	public MemberResponseDto registerOrUpdateProfile(MemberProfileRequestDto dto, Long userId) {
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
			member.getGender() != null ? member.getGender().name() : null,
			member.getBirthDate() != null ? member.getBirthDate().getValue().toString() : null,
			member.getOccupation() != null ? member.getOccupation().name() : null,
			member.getOccupationDetail(),
			member.getLastSurveySubmittedAt()
		);
	}

	@Transactional(readOnly = true)
	public MemberResponseDto getMemberById(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		return toDto(member);
	}

	@Transactional
	public void updateProfileImage(Long memberId, String profileImageUrl) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Profile updatedProfile = member.getProfile().update(null, profileImageUrl);
		member.updateProfile(updatedProfile);
	}

}