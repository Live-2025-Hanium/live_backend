package com.example.live_backend.domain.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.security.PrincipalDetails;

@Service
public class AuthenticationService {
	
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	public Long getUserId() {
		Authentication authentication = getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}
		
		Object principal = authentication.getPrincipal();

		if (principal instanceof PrincipalDetails principalDetails) {
			return principalDetails.getMemberId();
		}

		if ("anonymousUser".equals(principal) || principal == null) {
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}

		throw new CustomException(ErrorCode.INVALID_TOKEN, 
			"Invalid principal type: " + principal.getClass().getSimpleName());
	}


	public PrincipalDetails getCurrentUser() {
		Authentication authentication = getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}
		
		Object principal = authentication.getPrincipal();
		
		if (principal instanceof PrincipalDetails principalDetails) {
			return principalDetails;
		}
		
		if ("anonymousUser".equals(principal) || principal == null) {
			throw new CustomException(ErrorCode.DENIED_UNAUTHORIZED_USER);
		}
		
		throw new CustomException(ErrorCode.INVALID_TOKEN, 
			"Invalid principal type: " + principal.getClass().getSimpleName());
	}

	public boolean hasRole(String role) {
		try {
			PrincipalDetails user = getCurrentUser();
			return user.getAuthorities().stream()
					.anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
		} catch (CustomException e) {
			return false;
		}
	}

	public boolean isAdmin() {
		return hasRole("ADMIN");
	}

	public boolean isUser() {
		return hasRole("USER");
	}

	public boolean isAuthenticated() {
		Authentication authentication = getAuthentication();
		return authentication != null && authentication.isAuthenticated() 
			&& !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
	}
}
