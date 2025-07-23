package com.example.live_backend.domain.auth.dto.response;

import com.example.live_backend.domain.auth.dto.AuthToken;

import lombok.Getter;

@Getter
public class TokensResponseDto {
	private final String accessToken;
	private final String refreshToken;

	public TokensResponseDto(AuthToken token) {
		this.accessToken = token.accessToken();
		this.refreshToken = token.refreshToken();
	}
}