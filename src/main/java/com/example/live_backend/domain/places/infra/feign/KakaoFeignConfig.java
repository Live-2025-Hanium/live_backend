package com.example.live_backend.domain.places.infra.feign;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class KakaoFeignConfig {

    @Value("${kakao.local.rest-api-key}")
    private String kakaoApiKey;

    @Bean
    public RequestInterceptor kakaoRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "KakaoAK " + kakaoApiKey);
        };
    }

    @Bean
    public Request.Options kakaoRequestOptions() {
        return new Request.Options(
            1000, TimeUnit.MILLISECONDS,  // connectTimeout
            2500, TimeUnit.MILLISECONDS,  // readTimeout
            true  // followRedirects
        );
    }

    @Bean
    public Logger.Level kakaoFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}