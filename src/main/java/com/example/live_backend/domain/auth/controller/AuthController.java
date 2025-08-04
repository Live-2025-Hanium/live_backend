package com.example.live_backend.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.example.live_backend.domain.auth.controller.docs.AuthControllerDocs;
import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.dto.request.LogoutRequestDto;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.dto.response.LoginResult;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.request.RefreshRequestDto;
import com.example.live_backend.domain.auth.dto.response.TokensResponseDto;
import com.example.live_backend.domain.auth.jwt.JwtUtil;
import com.example.live_backend.domain.auth.jwt.TokenInfo;
import com.example.live_backend.domain.auth.service.AuthenticationFacade;
import com.example.live_backend.domain.auth.token.service.RefreshTokenService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.annotation.PublicApi;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthControllerDocs {

	private final AuthenticationFacade authenticationFacade;

	private final RefreshTokenService refreshTokenService;

	private final JwtUtil jwtUtil;

	@Override
	@PublicApi(reason = "로그인은 누구나 접근 가능해야 하는 공개 API")
	@PostMapping("/kakao/login")
	public ResponseHandler<LoginResponseDto> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
		LoginResult result = authenticationFacade.processKakaoLogin(request);
		return ResponseHandler.success(result.getResponse());
	}

	@Override
	@PublicApi(reason = "토큰 갱신은 만료된 액세스 토큰도 처리해야 하므로 인증 우회 필요")
	@PostMapping("/refresh")
	public ResponseHandler<TokensResponseDto> refresh(@RequestBody RefreshRequestDto request) {
		AuthToken tokens = refreshTokenService.refresh(request.getRefreshToken());
		TokensResponseDto response = new TokensResponseDto(tokens);
		return ResponseHandler.success(response);
	}

	@Override
	@PublicApi(reason = "로그아웃은 만료된 토큰도 처리해야 하므로 인증 우회 필요")
	@PostMapping("/logout")
	public ResponseHandler<Void> logout(@RequestBody LogoutRequestDto request) {
		try {
			TokenInfo tokenInfo = jwtUtil.validateAndExtract(request.getRefreshToken());
			Long userId = tokenInfo.getUserId();

			try {
				refreshTokenService.deleteRefreshToken(userId);
				log.info("로그아웃 성공: userId={}, 토큰만료여부={}", userId, tokenInfo.isExpired());
			} catch (Exception deleteException) {
				log.warn("리프레시 토큰 삭제 실패 (로그아웃은 정상 처리): userId={}, error={}", 
					userId, deleteException.getMessage());
			}
			
			return ResponseHandler.success(null);
		} catch (Exception e) {
			log.warn("로그아웃 실패 - JWT 파싱 오류: error={}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
}
