package com.example.live_backend.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.example.live_backend.domain.auth.controller.docs.DemoAuthControllerDocs;
import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.dto.response.AuthUserDto;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.token.service.RefreshTokenService;
import com.example.live_backend.domain.auth.util.AuthTokenGenerator;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.annotation.PublicApi;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class DemoAuthController implements DemoAuthControllerDocs {

    private static final String DEMO_USER_OAUTH_ID = "DEMO_USER_001";

    private final MemberRepository memberRepository;
    private final AuthTokenGenerator tokenGenerator;
    private final RefreshTokenService refreshTokenService;

    @Override
    @PublicApi(reason = "공모전 심사용 테스트 로그인은 인증 없이 접근 가능해야 함")
    @PostMapping("/demo/login")
    public ResponseHandler<LoginResponseDto> demoLogin() {
        log.info("테스트 로그인 요청");

        Member demoUser = memberRepository.findByOauthId(DEMO_USER_OAUTH_ID)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        AuthToken tokens = tokenGenerator.generate(
            demoUser.getId(),
            demoUser.getOauthId(),
            demoUser.getRole().name()
        );

        refreshTokenService.saveRefreshToken(demoUser.getId(), tokens.refreshToken());

        AuthUserDto authUser = AuthUserDto.builder()
            .id(demoUser.getId())
            .oauthId(demoUser.getOauthId())
            .email(demoUser.getEmail())
            .nickname(demoUser.getProfile().getNickname())
            .profileImageUrl(demoUser.getProfile().getProfileImageUrl())
            .role(demoUser.getRole())
            .isNewUser(true)
            .build();

        LoginResponseDto response = LoginResponseDto.from(authUser, tokens.accessToken(), tokens.refreshToken());

        log.info("테스트 로그인 성공: userId={}", demoUser.getId());

        return ResponseHandler.success(response);
    }
}
