package com.example.live_backend.domain.auth.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.live_backend.domain.auth.AuthToken;
import com.example.live_backend.domain.auth.AuthTokenGenerator;
import com.example.live_backend.domain.auth.jwt.JwtConstants;
import com.example.live_backend.domain.auth.jwt.JwtUtil;
import com.example.live_backend.domain.auth.jwt.TokenInfo;
import com.example.live_backend.domain.auth.token.entity.RefreshToken;
import com.example.live_backend.domain.auth.token.repository.RefreshTokenRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository repository;
	private final JwtUtil jwtUtil;
	private final AuthTokenGenerator generator;

	public RefreshToken getUserRefreshToken(Long userId) {
		return repository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.MISSING_REFRESH_TOKEN));
	}

	@Transactional
	public AuthToken refresh(String refreshToken) {

		TokenInfo tokenInfo = jwtUtil.validateAndExtract(refreshToken);

		if (tokenInfo.isExpired()) {
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		}

		if (!JwtConstants.REFRESH_TOKEN_CATEGORY.equals(tokenInfo.getCategory())) {
			throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
		}

		Long userId = tokenInfo.getUserId();
		String expected = getUserRefreshToken(userId).getToken();
		if (!expected.equals(refreshToken)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}

		AuthToken tokens = generator.generate(userId, tokenInfo.getOauthId(), tokenInfo.getRole());
		saveRefreshToken(userId, tokens.refreshToken());
		return tokens;
	}

	@Transactional
	public void saveRefreshToken(Long userId, String refreshToken) {
		repository.findById(userId)
			.ifPresentOrElse(
				rt -> rt.updateToken(refreshToken),
				() -> repository.save(new RefreshToken(userId, refreshToken))
			);
	}

	@Transactional
	public void deleteRefreshToken(Long userId) {
		repository.deleteById(userId);
	}
}