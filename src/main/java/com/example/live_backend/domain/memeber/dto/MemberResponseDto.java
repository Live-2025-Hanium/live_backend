package com.example.live_backend.domain.memeber.dto;

import lombok.Getter;

@Getter
public class MemberResponseDto {
	private Long id;
	private String nickname;
	private String profileImageUrl;
	private String gender;
	private String birthDate;
	private String occupation;
	private String occupationDetail;

	public MemberResponseDto(Long id,
		String nickname,
		String profileImageUrl,
		String gender,
		String birthDate,
		String occupation,
		String occupationDetail) {
		this.id = id;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.gender = gender;
		this.birthDate = birthDate;
		this.occupation = occupation;
		this.occupationDetail = occupationDetail;
	}
}
