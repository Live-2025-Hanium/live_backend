package com.example.live_backend.infra.kakao.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 카카오 Feign 클라이언트 활성화 설정
 * 각 Feign 클라이언트는 자체 Config 클래스를 사용
 */
@Configuration
@EnableFeignClients(basePackages = {
    "com.example.live_backend.infra.kakao.feign",
    "com.example.live_backend.infra.kakao.oauth.feign"
})
public class KakaoFeignConfig {
    // 전역 Bean 없음 - 각 Feign 클라이언트가 자체 설정 사용
}