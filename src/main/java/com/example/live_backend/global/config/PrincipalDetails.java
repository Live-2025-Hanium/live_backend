package com.example.live_backend.global.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails 구현 클래스
 * InMemoryUserDetailsManager에서 사용되는 Mock 사용자 정보
 */
public class PrincipalDetails implements UserDetails {

	private final Long userId;
	private final String role;

	public PrincipalDetails(Long userId, String role) {
		this.userId = userId;
		this.role = role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return "{noop}mock-password";  // Mock 비밀번호
	}

	@Override
	public String getUsername() {
		return userId.toString();  // userId를 username으로 사용
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// 추가 편의 메서드
	public Long getUserId() {
		return userId;
	}

	public String getRole() {
		return role;
	}
}