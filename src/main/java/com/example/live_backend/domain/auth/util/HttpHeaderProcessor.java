package com.example.live_backend.domain.auth.util;

import com.example.live_backend.domain.auth.jwt.JwtConstants;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

/**
 * HTTP 헤더 처리의 책이을 가진 유틸리티 클래스
 * 기존 코드에서 중복되던 헤더 검증 및 토큰 추출 로직을 통합했습니다.
 */
public final class HttpHeaderProcessor {
	
	private HttpHeaderProcessor() {

	}
	
	/**
	 * Authorization 헤더에서 토큰 추출

	 */
	public static String extractToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(JwtConstants.BEARER_PREFIX)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		
		if (authorizationHeader.length() <= JwtConstants.BEARER_PREFIX_LENGTH) {
			throw new CustomException(ErrorCode.MISSING_TOKEN);
		}
		
		return authorizationHeader.substring(JwtConstants.BEARER_PREFIX_LENGTH);
	}
	
	/**
	 * Authorization 헤더가 Bearer 토큰 형식인지 확인
	 */
	public static boolean isBearerToken(String authorizationHeader) {
		return authorizationHeader != null 
			&& authorizationHeader.startsWith(JwtConstants.BEARER_PREFIX)
			&& authorizationHeader.length() > JwtConstants.BEARER_PREFIX_LENGTH;
	}

	/**
	 * Bearer 토큰 형식으로 헤더 생성
	 */
	public static String createBearerToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new IllegalArgumentException("Token cannot be null or empty");
		}
		return JwtConstants.BEARER_PREFIX + token;
	}
} 