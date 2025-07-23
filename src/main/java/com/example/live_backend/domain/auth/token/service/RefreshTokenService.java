package com.example.live_backend.domain.auth.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.live_backend.domain.auth.dto.AuthToken;
import com.example.live_backend.domain.auth.util.AuthTokenGenerator;
import com.example.live_backend.domain.auth.jwt.JwtTokenValidator;
import com.example.live_backend.domain.auth.jwt.TokenInfo;
import com.example.live_backend.domain.auth.token.entity.RefreshToken;
import com.example.live_backend.domain.auth.token.repository.RefreshTokenRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository repository;

	private final JwtTokenValidator jwtTokenValidator;

	private final AuthTokenGenerator generator;

	public RefreshToken getUserRefreshToken(Long userId) {
		return repository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.MISSING_REFRESH_TOKEN));
	}


	@Transactional
	public AuthToken refresh(String refreshToken) {

		TokenInfo tokenInfo = jwtTokenValidator.validateRefreshToken(refreshToken);

		Long userId = tokenInfo.getUserId();
		String expectedToken = getUserRefreshToken(userId).getToken();
		
		if (!expectedToken.equals(refreshToken)) {
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