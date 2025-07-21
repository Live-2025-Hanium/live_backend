package com.example.live_backend.domain.auth.jwt;

import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
	protected void doFilterInternal(HttpServletRequest req,
		HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {

		String header = req.getHeader(JwtConstants.AUTHORIZATION_HEADER);
		if (header == null || !header.startsWith(JwtConstants.BEARER_PREFIX)) {
			chain.doFilter(req, res);
			return;
		}
		String token = header.substring(JwtConstants.BEARER_PREFIX_LENGTH);

		try {
			TokenInfo tokenInfo = jwtUtil.validateAndExtract(token);

			if (tokenInfo.isExpired()) {
				sendError(res, ErrorCode.EXPIRED_TOKEN);
				return;
			}

			if (!JwtConstants.ACCESS_TOKEN_CATEGORY.equals(tokenInfo.getCategory())) {
				sendError(res, ErrorCode.INVALID_TOKEN_CATEGORY);
				return;
			}

			Authentication auth = new UsernamePasswordAuthenticationToken(
				tokenInfo.getUserId(),
				null,
				Collections.singleton(new SimpleGrantedAuthority(tokenInfo.getRole()))
			);
			SecurityContextHolder.getContext().setAuthentication(auth);

		} catch (Exception e) {
			sendError(res, ErrorCode.INVALID_TOKEN);
			return;
		}

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