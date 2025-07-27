package com.example.live_backend.domain.auth.util;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.jwt.JwtConstants;
import com.example.live_backend.domain.auth.jwt.JwtUtil;

@Component
@RequiredArgsConstructor
public class AuthTokenGenerator {

	private final JwtUtil jwtUtil;

	@Value("${spring.jwt.accessToken.expiration}")
	private Long accessTokenExpiration;
	@Value("${spring.jwt.refreshToken.expiration}")
	private Long refreshTokenExpiration;

	public AuthToken generate(Long userId, String oauthId, String role) {
		String accessToken = jwtUtil.createJwt(
			JwtConstants.ACCESS_TOKEN_CATEGORY,
			userId, oauthId, role, accessTokenExpiration);
		String refreshToken = jwtUtil.createJwt(
			JwtConstants.REFRESH_TOKEN_CATEGORY,
			userId, oauthId, role, refreshTokenExpiration);
		return new AuthToken(accessToken, refreshToken);
	}
}