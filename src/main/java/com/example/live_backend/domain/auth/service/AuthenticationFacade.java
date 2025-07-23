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
        // 1. 사용자 로그인 또는 회원가입 처리
        AuthUserDto user = memberService.loginOrRegister(request);
        
        // 2. 토큰 생성
        AuthToken tokens = tokenGenerator.generate(
            user.getId(), user.getKakaoId(), user.getRole().name()
        );
        
        // 3. 리프레시 토큰 저장
        refreshTokenService.saveRefreshToken(user.getId(), tokens.refreshToken());
        
        // 4. 응답 DTO 생성 - 정적 팩토리 메서드 사용
        return LoginResponseDto.from(user, tokens);
    }
} 