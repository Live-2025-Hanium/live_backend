package com.example.live_backend.domain.memeber.dto;

import lombok.Getter;

@Getter
public class MemberProfileRequestDto {
	private String nickname;
	private String profileImageUrl;
	private String gender;
	private Integer birthYear;
	private Integer birthMonth;
	private Integer birthDay;
	private String occupation;
	private String occupationDetail;
}