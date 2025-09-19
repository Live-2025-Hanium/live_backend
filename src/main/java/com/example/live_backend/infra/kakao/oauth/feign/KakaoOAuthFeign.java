package com.example.live_backend.infra.kakao.oauth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.live_backend.infra.kakao.oauth.dto.KakaoTokenResponse;

@FeignClient(
    name = "kakao-oauth",
    url = "https://kauth.kakao.com",
    configuration = KakaoOAuthFeignConfig.class
)
public interface KakaoOAuthFeign {

    @PostMapping("/oauth/token")
    KakaoTokenResponse getToken(
        @RequestParam("grant_type") String grantType,
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("code") String code,
        @RequestParam("redirect_uri") String redirectUri
    );
}