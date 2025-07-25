package com.example.live_backend.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

@Tag(name = "Authentication", description = "인증 및 토큰 관리 API")
@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationFacade authenticationFacade;

	private final RefreshTokenService refreshTokenService;

	@PostMapping("/kakao/login")
	@Operation(
		summary = "카카오 소셜 로그인", 
		description = "카카오 OAuth2 인증을 통한 로그인 또는 회원가입을 처리합니다. 신규 사용자인 경우 자동으로 회원가입이 됩니다.",
		responses = {
			@ApiResponse(
				responseCode = "200", 
				description = "로그인 성공",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = LoginResponseDto.class)
				),
				headers = {
					@io.swagger.v3.oas.annotations.headers.Header(
						name = "Authorization",
						description = "Bearer 액세스 토큰",
						schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
					)
				}
			),
			@ApiResponse(
				responseCode = "400", 
				description = "잘못된 요청 데이터",
				content = @Content(schema = @Schema())
			),
			@ApiResponse(
				responseCode = "500", 
				description = "서버 내부 오류",
				content = @Content(schema = @Schema())
			)
		}
	)
	public ResponseEntity<LoginResponseDto> kakaoLogin(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "카카오 로그인 요청 정보",
			required = true,
			content = @Content(
				schema = @Schema(implementation = KakaoLoginRequestDto.class)
			)
		)
		@RequestBody KakaoLoginRequestDto request
	) {
		LoginResponseDto response = authenticationFacade.processKakaoLogin(request);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, 
					HttpHeaderProcessor.createBearerToken(response.getAccessToken()));
		
		return ResponseEntity.ok()
			.headers(headers)
			.body(response);
	}

	@PostMapping("/refresh")
	@Operation(
		summary = "액세스 토큰 갱신",
		description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "토큰 갱신 성공",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = TokensResponseDto.class)
				),
				headers = {
					@io.swagger.v3.oas.annotations.headers.Header(
						name = "Authorization",
						description = "새로운 Bearer 액세스 토큰",
						schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
					)
				}
			),
			@ApiResponse(
				responseCode = "401",
				description = "유효하지 않은 리프레시 토큰",
				content = @Content(schema = @Schema())
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 내부 오류",
				content = @Content(schema = @Schema())
			)
		}
	)
	public ResponseEntity<TokensResponseDto> refresh(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "토큰 갱신 요청 정보",
			required = true,
			content = @Content(
				schema = @Schema(implementation = RefreshRequestDto.class)
			)
		)
		@RequestBody RefreshRequestDto req
	) {
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
