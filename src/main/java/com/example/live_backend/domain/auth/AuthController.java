package com.example.live_backend.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.live_backend.domain.auth.dto.AuthUserDto;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.dto.request.RefreshRequestDto;
import com.example.live_backend.domain.auth.dto.response.TokensResponseDto;
import com.example.live_backend.domain.auth.token.service.RefreshTokenService;
import com.example.live_backend.domain.memeber.service.MemberService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final MemberService memberService;
	private final AuthTokenGenerator tokenGenerator;
	private final RefreshTokenService refreshService;

	@PostMapping("/kakao/login")
	public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {

		AuthUserDto user = memberService.loginOrRegister(request);

		AuthToken tokens = tokenGenerator.generate(
			user.getId(), user.getKakaoId(), user.getRole().name()
		);

		refreshService.saveRefreshToken(user.getId(), tokens.refreshToken());

		LoginResponseDto.UserInfoDto userInfo = LoginResponseDto.UserInfoDto.builder()
			.id(user.getId())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImageUrl())
			.role(user.getRole().name())
			.build();

		LoginResponseDto response = LoginResponseDto.builder()
			.accessToken(tokens.accessToken())
			.refreshToken(tokens.refreshToken())
			.user(userInfo)
			.isNewUser(user.isNewUser())
			.build();

		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokensResponseDto> refresh(@RequestBody RefreshRequestDto req) {
		AuthToken tokens = refreshService.refresh(req.getRefreshToken());
		return ResponseEntity.ok(new TokensResponseDto(tokens));
	}
}
