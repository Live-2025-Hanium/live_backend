package com.example.live_backend.domain.auth.v2.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthCallbackRequest(
    @NotBlank(message = "인가 코드는 필수입니다.")
    String code,

    @NotBlank(message = "리다이렉트 URI는 필수입니다.")
    String redirectUri
) {}