package com.example.live_backend.domain.memeber.dto;

import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

@Getter
public class MemberProfileRequestDto {
	@NotBlank(message = "닉네임은 필수입니다")
	private String nickname;
	
	private String profileImageUrl;
	
	@NotBlank(message = "성별은 필수입니다")
	private String gender;
	
	@NotNull(message = "생년은 필수입니다")
	@Min(value = 1900, message = "생년은 1900년 이상이어야 합니다")
	@Max(value = 2024, message = "생년은 2024년 이하여야 합니다")
	private Integer birthYear;
	
	@NotNull(message = "생월은 필수입니다")
	@Min(value = 1, message = "생월은 1 이상이어야 합니다")
	@Max(value = 12, message = "생월은 12 이하여야 합니다")
	private Integer birthMonth;
	
	@NotNull(message = "생일은 필수입니다")
	@Min(value = 1, message = "생일은 1 이상이어야 합니다")
	@Max(value = 31, message = "생일은 31 이하여야 합니다")
	private Integer birthDay;
	
	@NotBlank(message = "직업은 필수입니다")
	private String occupation;
	
	private String occupationDetail;
}