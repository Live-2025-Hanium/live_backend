package com.example.live_backend.infra.kakao.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(
    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("expires_in")
    Integer expiresIn,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("refresh_token_expires_in")
    Integer refreshTokenExpiresIn,

    @JsonProperty("scope")
    String scope
) {}