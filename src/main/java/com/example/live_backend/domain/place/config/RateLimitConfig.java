package com.example.live_backend.domain.place.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${place.rate-limit.capacity:10}")
    private int capacity;
    
    @Value("${place.rate-limit.refill-tokens:5}")
    private int refillTokens;
    
    @Value("${place.rate-limit.refill-period-seconds:1}")
    private int refillPeriodSeconds;
    
    @Value("${place.rate-limit.cache-max-size:10000}")
    private int cacheMaxSize;
    
    @Value("${place.rate-limit.cache-expire-hours:1}")
    private int cacheExpireHours;

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterAccess(Duration.ofHours(1))
        .recordStats()
        .build();
    
    @Bean
    public Cache<String, Bucket> rateLimitCache() {
        return cache;
    }

    public Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            capacity, 
            Refill.intervally(refillTokens, Duration.ofSeconds(refillPeriodSeconds))
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    public Bucket resolveBucket(String key) {
        return cache.get(key, k -> createNewBucket());
    }
}