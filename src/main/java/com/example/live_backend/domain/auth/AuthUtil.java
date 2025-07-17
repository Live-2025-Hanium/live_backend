package com.example.live_backend.domain.auth;

import com.example.live_backend.domain.auth.jwt.JwtConstants;

public class AuthUtil {
	public static String extractToken(String header) {
		return header.substring(JwtConstants.BEARER_PREFIX_LENGTH);
	}
}