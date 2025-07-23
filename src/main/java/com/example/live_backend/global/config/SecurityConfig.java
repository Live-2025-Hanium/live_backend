package com.example.live_backend.global.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.live_backend.domain.auth.jwt.JwtFilter;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	/**
	 * 스웨거 및 정적 리소스 전용 체인
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain staticResourcesChain(HttpSecurity http) throws Exception {
		http
			// 정적 리소스 매칭
			.securityMatcher(
				PathRequest.toStaticResources().atCommonLocations()
			)
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable) // Flutter 앱에서는 CORS 불필요
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.sessionManagement(mgmt ->
				mgmt.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			);
		return http.build();
	}

	/**
	 * API 요청용 체인: JWT 필터 적용
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/**")
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable) // Flutter 앱에서는 CORS 불필요
			.sessionManagement(mgmt ->
				mgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			// JWT 필터 적용
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			// 엔드포인트별 접근 제어
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/auth/login",
					"/api/auth/kakao/login",
					"/api/auth/refresh",
					"/api/members/nickname/check"
				).permitAll()
				.anyRequest().authenticated()
			);
		return http.build();
	}

	// Flutter 앱에서는 CORS 설정이 불필요합니다.
	// 웹 브라우저가 아닌 네이티브 앱이므로 브라우저의 동일 출처 정책에 영향받지 않습니다!
}