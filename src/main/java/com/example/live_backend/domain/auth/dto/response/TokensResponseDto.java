package com.example.live_backend.domain.auth.dto.response;

import com.example.live_backend.domain.auth.dto.AuthToken;

import lombok.Getter;

@Getter
public class TokensResponseDto {
	private final String refreshToken;

	public TokensResponseDto(AuthToken token) {
		this.refreshToken = token.refreshToken();
	}
}