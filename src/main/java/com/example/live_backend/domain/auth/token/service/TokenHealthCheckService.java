package com.example.live_backend.domain.auth.token.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.live_backend.domain.auth.jwt.JwtUtil;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class TokenHealthCheckService {

	private final JwtUtil jwtUtil;

	public void healthCheck(String token) {
		if (jwtUtil.isExpired(token)) {
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		}
	}
}