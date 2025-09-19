package com.example.live_backend.domain.auth.v2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.example.live_backend.domain.auth.v2.controller.docs.AuthControllerV2Docs;
import com.example.live_backend.domain.auth.v2.dto.request.OAuthCallbackRequest;
import com.example.live_backend.domain.auth.v2.service.KakaoAuthService;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.dto.response.LoginResult;
import com.example.live_backend.domain.auth.service.AuthenticationFacade;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.annotation.PublicApi;

@RestController
@RequestMapping(path = "/api/v2/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AuthControllerV2 implements AuthControllerV2Docs {

    private final KakaoAuthService kakaoAuthService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @PublicApi(reason = "OAuth 로그인은 누구나 접근 가능해야 하는 공개 API")
    @PostMapping("/kakao/callback")
    public ResponseHandler<LoginResponseDto> kakaoOAuthCallback(@Valid @RequestBody OAuthCallbackRequest request) {
        log.info("카카오 OAuth 콜백 요청: redirectUri={}", request.redirectUri());

        KakaoLoginRequestDto kakaoUser = kakaoAuthService.processOAuthLogin(
            request.code(),
            request.redirectUri()
        );
        LoginResult result = authenticationFacade.processKakaoLogin(kakaoUser);

        return ResponseHandler.success(result.getResponse());
    }
}