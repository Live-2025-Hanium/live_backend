package com.example.live_backend.domain.places.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class ResilienceConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)  // 실패율 50% 이상시 열림
            .waitDurationInOpenState(Duration.ofSeconds(30))  // 열린 상태 유지 시간
            .slidingWindowSize(10)  // 슬라이딩 윈도우 크기
            .permittedNumberOfCallsInHalfOpenState(3)  // Half-Open 상태에서 허용할 호출 수
            .slowCallRateThreshold(50)  // 느린 호출 비율 임계값
            .slowCallDurationThreshold(Duration.ofSeconds(3))  // 느린 호출 기준 시간
            .minimumNumberOfCalls(5)  // 최소 호출 수
            .automaticTransitionFromOpenToHalfOpenEnabled(true)  // 자동 전환 활성화
            .build();
        
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }
    
    @Bean
    public CircuitBreaker kakaoLocalCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("kakaoLocal");
    }
    
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(2)  // 최대 재시도 횟수 (원본 포함)
            .waitDuration(Duration.ofMillis(500))  // 재시도 간격
            .ignoreExceptions(IllegalArgumentException.class)  // 재시도하지 않을 예외
            .retryExceptions(TimeoutException.class)  // 재시도할 예외
            .build();
        
        return RetryRegistry.of(retryConfig);
    }
    
    @Bean
    public Retry kakaoLocalRetry(RetryRegistry registry) {
        return registry.retry("kakaoLocal");
    }
}