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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.live_backend.domain.auth.jwt.JwtFilter;

import java.util.Arrays;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	/**
	 * 웹 애플리케이션 V2 API용 체인 (CORS 활성화)
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain webApiChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/v2/**")
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(mgmt ->
				mgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v2/auth/kakao/callback"
				).permitAll()
				.anyRequest().authenticated()
			);
		return http.build();
	}

	/**
	 * 스웨거 및 정적 리소스 전용 체인
	 */
	@Bean
	@Order(2)
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
	@Order(3)
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
			// 메타 어노테이션(@PublicApi, @AuthenticatedApi)으로 제어하는 엔드포인트는
			// 여기서는 authenticated()로 설정하고, 실제 권한은 메서드 레벨에서 결정
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/auth/kakao/login",
					"/api/auth/refresh",
					"/api/members/nickname/check"
				).permitAll()
				// 나머지는 기본적으로 인증 필요 (메타 어노테이션이 최종 결정)
				.anyRequest().authenticated()
			);
		return http.build();
	}


	/**
	 * CORS 설정 (웹 애플리케이션용)
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 허용할 Origin 설정
		configuration.setAllowedOrigins(Arrays.asList(
			"http://localhost:3000",
			"http://127.0.0.1:3000"
		));

		// 허용할 HTTP 메서드
		configuration.setAllowedMethods(Arrays.asList(
			"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
		));

		// 허용할 헤더
		configuration.setAllowedHeaders(Arrays.asList(
			"Authorization",
			"Content-Type",
			"X-Requested-With",
			"Accept",
			"Origin"
		));

		// 노출할 헤더
		configuration.setExposedHeaders(Arrays.asList(
			"Authorization",
			"Content-Type"
		));

		// 크리덴셜 허용
		configuration.setAllowCredentials(true);

		// Preflight 요청 캐시 시간 (초)
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/v2/**", configuration);

		return source;
	}
}