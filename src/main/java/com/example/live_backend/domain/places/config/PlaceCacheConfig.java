package com.example.live_backend.domain.places.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PlaceCacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 기본 캐시 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(100));
        
        // 캐시별 개별 설정
        cacheManager.registerCustomCache("placeSearch",
            Caffeine.newBuilder()
                .expireAfterWrite(90, TimeUnit.SECONDS)  // TTL 90초
                .maximumSize(500)  // 최대 500개 엔트리
                .recordStats()  // 통계 기록
                .build());
        
        cacheManager.registerCustomCache("placeNearby",
            Caffeine.newBuilder()
                .expireAfterWrite(90, TimeUnit.SECONDS)  // TTL 90초
                .maximumSize(500)  // 최대 500개 엔트리
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("placeDetail",
            Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)  // TTL 10분
                .maximumSize(200)  // 최대 200개 엔트리
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("suggest",
            Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)  // TTL 60초
                .maximumSize(300)  // 최대 300개 엔트리
                .recordStats()
                .build());
        
        return cacheManager;
    }
}