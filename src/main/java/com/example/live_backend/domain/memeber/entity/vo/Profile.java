package com.example.live_backend.domain.memeber.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"nickname", "profileImageUrl"})
public class Profile {

	private static final int MIN_NICKNAME_LENGTH = 2;
	private static final int MAX_NICKNAME_LENGTH = 20;
	
	// 닉네임 정책: 한글, 영문(대소문자), 숫자만 허용
	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

	@Column(name = "nickname", nullable = false, length = 20)
	private String nickname;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Builder
	public Profile(String nickname, String profileImageUrl) {
		validateNickname(nickname);
		this.nickname = nickname.trim();
		this.profileImageUrl = profileImageUrl;
	}

	public Profile update(String newNickname, String newProfileImageUrl) {
		String updatedNickname = Optional.ofNullable(newNickname)
			.map(String::trim)
			.filter(StringUtils::hasText)
			.orElse(this.nickname);

		String updatedImageUrl = Optional.ofNullable(newProfileImageUrl)
			.map(String::trim)
			.filter(StringUtils::hasText)
			.orElse(this.profileImageUrl);

		if (updatedNickname.equals(this.nickname)
			&& updatedImageUrl.equals(this.profileImageUrl)) {
			return this;
		}

		return Profile.builder()
			.nickname(updatedNickname)
			.profileImageUrl(updatedImageUrl)
			.build();
	}

	private void validateNickname(String nickname) {
		if (!StringUtils.hasText(nickname)) {
			throw new CustomException(ErrorCode.MEMBER_NICKNAME_REQUIRED);
		}
		
		// 공백 검사 (trim 하기 전에 검사)
		if (nickname.contains(" ")) {
			throw new CustomException(ErrorCode.MEMBER_NICKNAME_SPACE_NOT_ALLOWED);
		}
		
		String trimmed = nickname.trim();
		
		// 길이 검사
		if (trimmed.length() < MIN_NICKNAME_LENGTH || trimmed.length() > MAX_NICKNAME_LENGTH) {
			throw new CustomException(ErrorCode.MEMBER_NICKNAME_LENGTH_INVALID);
		}
		
		// 문자 구성 검사 (한글, 영문, 숫자만 허용)
		if (!NICKNAME_PATTERN.matcher(trimmed).matches()) {
			throw new CustomException(ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID);
		}
	}
}