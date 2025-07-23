package com.example.live_backend.domain.auth.jwt;

import com.example.live_backend.domain.auth.util.HttpHeaderProcessor;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtTokenValidator jwtTokenValidator;
	private final ObjectMapper objectMapper;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().startsWith("/api/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
		FilterChain chain) throws ServletException, IOException {

		String header = req.getHeader(JwtConstants.AUTHORIZATION_HEADER);
		

		if (!HttpHeaderProcessor.isBearerToken(header)) {
			chain.doFilter(req, res);
			return;
		}

		try {

			String token = HttpHeaderProcessor.extractToken(header);

			TokenInfo tokenInfo = jwtTokenValidator.validateAccessToken(token);

			PrincipalDetails principalDetails = new PrincipalDetails(
				tokenInfo.getUserId(),
				tokenInfo.getOauthId(),
				tokenInfo.getRole(),
				null,
				null
			);

			Authentication auth = new UsernamePasswordAuthenticationToken(
				principalDetails,
				null,
				principalDetails.getAuthorities()
			);
			SecurityContextHolder.getContext().setAuthentication(auth);

		} catch (CustomException e) {
			sendError(res, e.getErrorCode());
			return;
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