package com.example.live_backend.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.request.RefreshRequestDto;
import com.example.live_backend.domain.auth.dto.response.TokensResponseDto;
import com.example.live_backend.domain.auth.service.AuthenticationFacade;
import com.example.live_backend.domain.auth.token.service.RefreshTokenService;
import com.example.live_backend.domain.auth.util.HttpHeaderProcessor;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationFacade authenticationFacade;

	private final RefreshTokenService refreshTokenService;

	@PostMapping("/kakao/login")
	public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
		LoginResponseDto response = authenticationFacade.processKakaoLogin(request);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, 
					HttpHeaderProcessor.createBearerToken(response.getAccessToken()));
		
		return ResponseEntity.ok()
			.headers(headers)
			.body(response);
	}


	@PostMapping("/refresh")
	public ResponseEntity<TokensResponseDto> refresh(@RequestBody RefreshRequestDto req) {
		AuthToken tokens = refreshTokenService.refresh(req.getRefreshToken());
		TokensResponseDto response = new TokensResponseDto(tokens);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, 
					HttpHeaderProcessor.createBearerToken(tokens.accessToken()));
		
		return ResponseEntity.ok()
			.headers(headers)
			.body(response);
	}
}
