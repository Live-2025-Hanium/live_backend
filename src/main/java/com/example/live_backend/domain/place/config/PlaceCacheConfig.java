package com.example.live_backend.domain.place.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PlaceCacheConfig {
    
    @Bean(name = "placeCacheManager")
    public CacheManager placeCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(100));

        cacheManager.registerCustomCache("placeSearch",
            Caffeine.newBuilder()
                .expireAfterWrite(90, TimeUnit.SECONDS)
                .maximumSize(500)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("placeNearby",
            Caffeine.newBuilder()
                .expireAfterWrite(90, TimeUnit.SECONDS)
                .maximumSize(500)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("placeDetail",
            Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(200)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("suggest",
            Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .maximumSize(300)
                .recordStats()
                .build());
        
        return cacheManager;
    }
}