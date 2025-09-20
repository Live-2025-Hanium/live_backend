package com.example.live_backend.infra.kakao.feign;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * KakaoLocalFeign 전용 설정
 * 카카오 로컬 API (장소 검색 등)에 사용
 */
public class KakaoLocalFeignConfig {

    @Value("${kakao.local.rest-api-key}")
    private String kakaoApiKey;

    @Bean
    public RequestInterceptor localApiInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "KakaoAK " + kakaoApiKey);
        };
    }

    @Bean
    public Request.Options localApiRequestOptions() {
        return new Request.Options(
            1000, TimeUnit.MILLISECONDS,  // connectTimeout
            2500, TimeUnit.MILLISECONDS,  // readTimeout
            true  // followRedirects
        );
    }

    @Bean
    public Logger.Level localApiLoggerLevel() {
        return Logger.Level.BASIC;
    }
}