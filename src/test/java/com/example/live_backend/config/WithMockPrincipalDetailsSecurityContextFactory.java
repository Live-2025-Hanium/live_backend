package com.example.live_backend.config;

import com.example.live_backend.global.security.PrincipalDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockPrincipalDetailsSecurityContextFactory implements WithSecurityContextFactory<WithMockPrincipalDetails> {
    
    @Override
    public SecurityContext createSecurityContext(WithMockPrincipalDetails annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        PrincipalDetails principal = new PrincipalDetails(
            annotation.memberId(),
            annotation.oauthId(),
            annotation.role(),
            annotation.nickname(),
            annotation.email()
        );
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );
        
        context.setAuthentication(auth);
        return context;
    }
}