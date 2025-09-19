package com.example.live_backend.infra.kakao.oauth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.live_backend.infra.kakao.oauth.dto.KakaoUserResponse;

@FeignClient(
    name = "kakao-user",
    url = "https://kapi.kakao.com",
    configuration = KakaoOAuthFeignConfig.class
)
public interface KakaoUserFeign {

    @GetMapping("/v2/user/me")
    KakaoUserResponse getUserInfo(
        @RequestHeader("Authorization") String authorization
    );
}