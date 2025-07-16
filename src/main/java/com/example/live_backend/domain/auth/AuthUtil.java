package com.example.live_backend.domain.auth;

public class AuthUtil {
	private static final int BEARER_PREFIX = 7;
	public static String extractToken(String header) {
		return header.substring(BEARER_PREFIX);
	}
}