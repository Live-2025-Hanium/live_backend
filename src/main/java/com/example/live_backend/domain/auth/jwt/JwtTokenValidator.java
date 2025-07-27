package com.example.live_backend.domain.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;


@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtUtil jwtUtil;


    public TokenInfo validateAndExtractTokenInfo(String token) {
        return jwtUtil.validateAndExtract(token);
    }


    public TokenInfo validateRefreshToken(String refreshToken) {
        TokenInfo tokenInfo = validateAndExtractTokenInfo(refreshToken);
        
        if (tokenInfo.isExpired()) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        if (!JwtConstants.REFRESH_TOKEN_CATEGORY.equals(tokenInfo.getCategory())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }

        return tokenInfo;
    }


    public TokenInfo validateAccessToken(String accessToken) {
        TokenInfo tokenInfo = validateAndExtractTokenInfo(accessToken);
        
        if (tokenInfo.isExpired()) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        if (!JwtConstants.ACCESS_TOKEN_CATEGORY.equals(tokenInfo.getCategory())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }

        return tokenInfo;
    }
} 