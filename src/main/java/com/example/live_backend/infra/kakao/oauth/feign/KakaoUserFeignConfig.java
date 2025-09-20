package com.example.live_backend.infra.kakao.oauth.feign;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class KakaoUserFeignConfig {

    @Bean
    public Request.Options kakaoUserRequestOptions() {
        return new Request.Options(
            5000, TimeUnit.MILLISECONDS,  // connectTimeout
            10000, TimeUnit.MILLISECONDS,  // readTimeout
            true  // followRedirects
        );
    }

    @Bean
    public Logger.Level kakaoUserFeignLoggerLevel() {
        return Logger.Level.FULL;
    }
}