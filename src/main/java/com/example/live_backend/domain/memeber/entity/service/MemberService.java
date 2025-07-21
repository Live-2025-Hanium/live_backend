package com.example.live_backend.domain.memeber.entity.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.live_backend.domain.auth.AuthenticationService;
import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.Occupation;
import com.example.live_backend.domain.memeber.entity.vo.BirthDate;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final AuthenticationService authService;

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
				//existing.updateDetails(gender, birthDate, occupation, detail);
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