package com.example.live_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .formLogin(formLogin -> formLogin.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger UI 관련 경로 모두 허용
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**", "/webjars/**", "/v3/api-docs.yaml").permitAll()
                
                // 헬스체크 및 정적 리소스 허용
                .requestMatchers("/ping", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // 관리자 전용 API
                .requestMatchers("/api/v1/surveys/admin/**").hasRole("ADMIN")
                // 기타 API는 인증 필요
                .requestMatchers("/api/**").authenticated()
                
                .anyRequest().permitAll()
            )
            // HTTP Basic 인증 활성화 (Swagger 테스트용)
            .httpBasic(httpBasic -> httpBasic.realmName("Mock Authentication"))
            .build();
    }


    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails mockUser = new PrincipalDetails(1L, "ROLE_USER");
        UserDetails mockAdmin = new PrincipalDetails(2L, "ROLE_ADMIN");
        return new InMemoryUserDetailsManager(mockUser, mockAdmin);
    }
} 