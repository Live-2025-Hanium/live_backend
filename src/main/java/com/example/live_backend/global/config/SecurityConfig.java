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

import com.example.live_backend.domain.auth.jwt.JwtFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	/**
	 * 정적 리소스 및 Swagger 전용 체인
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain publicResourcesChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher(req ->
				PathRequest.toStaticResources().atCommonLocations().matches(req) ||
				req.getServletPath().startsWith("/swagger-ui") ||
				req.getServletPath().startsWith("/v3/api-docs") ||
				req.getServletPath().equals("/v3/api-docs")
			)
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.sessionManagement(mgmt ->
				mgmt.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			);
		return http.build();
	}

	/**
	 * API 요청 통합 체인 (앱 + 웹 공통)
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/**")
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(mgmt ->
				mgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			.cors(cors -> cors.configurationSource(request -> createCorsConfiguration()))
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(auth -> auth



				// 나머지는 인증 필요 (세부 권한은 @PublicApi 등 메타 어노테이션으로)
				.anyRequest().authenticated()
			);
		return http.build();
	}

	private CorsConfiguration createCorsConfiguration() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(List.of(
			"http://localhost:3000",
			"http://127.0.0.1:3000"
		));

		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of(
			"Authorization",
			"Content-Type"
		));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		return configuration;
	}
}