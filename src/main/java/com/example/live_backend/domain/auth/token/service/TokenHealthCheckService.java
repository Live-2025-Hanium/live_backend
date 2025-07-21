package com.example.live_backend.domain.auth.token.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.live_backend.domain.auth.jwt.JwtUtil;
import com.example.live_backend.domain.auth.jwt.TokenInfo;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class TokenHealthCheckService {

	private final JwtUtil jwtUtil;

	public void healthCheck(String token) {
		try {
			TokenInfo tokenInfo = jwtUtil.validateAndExtract(token);
			
			if (tokenInfo.isExpired()) {
				throw new CustomException(ErrorCode.EXPIRED_TOKEN);
			}
			if (!tokenInfo.isValid()) {
				throw new CustomException(ErrorCode.INVALID_TOKEN);
			}
			
		} catch (Exception e) {
			if (e instanceof CustomException) {
				throw e; // 이미 정의된 커스텀 예외는 그대로 전파한다
			}
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
}