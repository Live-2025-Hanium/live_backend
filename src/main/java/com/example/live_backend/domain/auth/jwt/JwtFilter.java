package com.example.live_backend.domain.auth.jwt;

import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().startsWith("/api/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
		FilterChain chain) throws ServletException, IOException {

		String header = req.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			chain.doFilter(req, res);
			return;
		}
		String token = header.substring(7);

		try {
			if (jwtUtil.isExpired(token)) {
				sendError(res, ErrorCode.EXPIRED_TOKEN);
				return;
			}
		} catch (ExpiredJwtException e) {
			sendError(res, ErrorCode.EXPIRED_TOKEN);
			return;
		}

		if (!"access_token".equals(jwtUtil.getCategory(token))) {
			sendError(res, ErrorCode.INVALID_TOKEN_CATEGORY);
			return;
		}

		Long userId = jwtUtil.getUserId(token);
		String role = jwtUtil.getRole(token);
		Authentication auth = new UsernamePasswordAuthenticationToken(
			userId, null, Collections.singleton((GrantedAuthority) () -> role)
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		chain.doFilter(req, res);
	}

	private void sendError(HttpServletResponse res, ErrorCode errorCode) throws IOException {
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");
		res.setStatus(errorCode.getHttpStatus().value());
		
		ResponseHandler<Object> errorResponse = ResponseHandler.error(errorCode);
		String body = objectMapper.writeValueAsString(errorResponse);
		
		try (PrintWriter w = res.getWriter()) {
			w.print(body);
		}
	}
} 