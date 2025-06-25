package com.example.live_backend.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    /**
     * 현재 Spring Security 인증된 사용자의 ID를 반환
     * InMemoryUserDetailsManager를 통해 인증된 사용자 정보 사용
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        
        // PrincipalDetails에서 사용자 ID 추출
        return Long.parseLong(authentication.getName());
    }

    /**
     * 현재 인증된 사용자의 권한을 확인
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
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
} 