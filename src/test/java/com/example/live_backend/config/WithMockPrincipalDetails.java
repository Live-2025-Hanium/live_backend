package com.example.live_backend.config;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockPrincipalDetailsSecurityContextFactory.class)
public @interface WithMockPrincipalDetails {
    long memberId() default 2L;
    String oauthId() default "test-oauth-id";
    String role() default "USER";
    String nickname() default "testUser";
    String email() default "test@example.com";
}