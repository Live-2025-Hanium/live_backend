package com.example.live_backend.domain.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Service
public class AuthenticationService {
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	public Long getUserId() {
		Authentication authentication = getAuthentication();

		if (authentication == null) {
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}
		
		Object principal = authentication.getPrincipal();

		if (principal instanceof Long) {
			return (Long) principal;
		}
		if ("anonymousUser".equals(principal) || principal == null) {
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}
		throw new CustomException(ErrorCode.INVALID_TOKEN, 
			"Invalid principal type: " + principal.getClass().getSimpleName());
	}
	
	/**
	 * 현재 인증된 사용자의 권한을 확인
	 */
	public boolean hasRole(String role) {
		Authentication authentication = getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
	}
	
	/**
	 * 현재 사용자가 관리자인지 확인
	 */
	public boolean isAdmin() {
		return hasRole("ADMIN");
	}
	
	/**
	 * 현재 사용자가 일반 사용자인지 확인  
	 */
	public boolean isUser() {
		return hasRole("USER");
	}
	
	/**
	 * 현재 사용자가 인증되었는지 확인
	 */
	public boolean isAuthenticated() {
		Authentication authentication = getAuthentication();
		return authentication != null && authentication.isAuthenticated() 
			&& !("anonymousUser".equals(authentication.getPrincipal()));
	}
}
