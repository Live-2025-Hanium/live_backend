package com.example.live_backend.global.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
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


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final String frontendUrl;
	private final String backendUrl;

	public SecurityConfig(
		JwtFilter jwtFilter,
		@Value("${app.frontend.base-url}") String frontendUrl,
		@Value("${app.backend.base-url}") String backendUrl
	) {
		this.jwtFilter = jwtFilter;
		this.frontendUrl = frontendUrl;
		this.backendUrl = backendUrl;
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
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(mgmt ->
				mgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			// JWT 필터 적용
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			// 엔드포인트별 접근 제어
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/auth/login",
					"/api/auth/refresh"
				).permitAll()
				.anyRequest().authenticated()
			);
		return http.build();
	}

	/**
	 * 공통 CORS 설정
	 */
	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(Arrays.asList(frontendUrl, backendUrl));
		cfg.setAllowedMethods(Arrays.asList(
			"GET","POST","PUT","DELETE","PATCH","OPTIONS","HEAD"
		));
		cfg.setAllowedHeaders(Arrays.asList(
			"Authorization","Content-Type","Accept","Origin"
		));
		cfg.setAllowCredentials(true);
		cfg.setMaxAge(3600L);
		cfg.setExposedHeaders(Arrays.asList("Authorization","Set-Cookie"));
		UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
		src.registerCorsConfiguration("/**", cfg);
		return src;
	}
}