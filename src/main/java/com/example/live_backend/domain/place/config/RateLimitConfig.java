package com.example.live_backend.domain.place.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    
    // IP별 버킷을 저장하는 캐시
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Bean
    public Map<String, Bucket> ipBucketCache() {
        return cache;
    }
    
    /**
     * IP별 레이트 리밋 버킷 생성
     * - 초당 5개 요청 허용 (5 rps)
     * - 버스트 10개까지 허용
     */
    public Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(5, Duration.ofSeconds(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    /**
     * IP 또는 토큰별 버킷 조회 (없으면 생성)
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }
}