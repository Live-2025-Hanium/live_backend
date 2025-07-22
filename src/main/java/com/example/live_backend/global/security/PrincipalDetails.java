package com.example.live_backend.global.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring Security에서 사용하는 사용자 인증 정보
 * JWT 인증 시 Authentication 객체에 저장되는 Principal
 */
@AllArgsConstructor
@Getter
public class PrincipalDetails implements UserDetails {

	private final Long memberId;
	private final String memberKey; // OAuth2 memberKey (예: "google 123456789")
	private final String role;
	private final String name; // 실제 사용자 이름
	private final String email; // 이메일 주소

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return null; // OAuth2 + JWT 사용 시 패스워드 불필요
	}

	@Override
	public String getUsername() {
		return memberKey;
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
}
