package com.example.live_backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.util.AuthTokenGenerator;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.response.AuthUserDto;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.token.service.RefreshTokenService;
import com.example.live_backend.domain.memeber.service.MemberService;


@Service
@RequiredArgsConstructor
public class AuthenticationFacade {

    private final MemberService memberService;
    private final AuthTokenGenerator tokenGenerator;
    private final RefreshTokenService refreshTokenService;


    @Transactional
    public LoginResponseDto processKakaoLogin(KakaoLoginRequestDto request) {

        AuthUserDto user = memberService.loginOrRegister(request);

        AuthToken tokens = tokenGenerator.generate(
            user.getId(), user.getOauthId(), user.getRole().name()
        );

        refreshTokenService.saveRefreshToken(user.getId(), tokens.refreshToken());

        return LoginResponseDto.from(user, tokens);
    }
} 