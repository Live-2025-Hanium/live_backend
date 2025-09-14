package com.example.live_backend.domain.place.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Place 도메인 메트릭 수집
 */
@Slf4j
@Component
public class PlaceMetrics {
    
    private final MeterRegistry registry;
    private final Counter searchCounter;
    private final Counter suggestCounter;
    private final Counter detailCounter;
    private final Counter apiErrorCounter;
    private final Timer apiResponseTimer;
    
    public PlaceMetrics(MeterRegistry registry) {
        this.registry = registry;
        
        // 카운터 초기화
        this.searchCounter = Counter.builder("place.search.count")
            .description("장소 검색 횟수")
            .register(registry);
            
        this.suggestCounter = Counter.builder("place.suggest.count")
            .description("자동완성 요청 횟수")
            .register(registry);
            
        this.detailCounter = Counter.builder("place.detail.count")
            .description("상세 조회 횟수")
            .register(registry);
            
        this.apiErrorCounter = Counter.builder("place.api.error")
            .description("API 에러 횟수")
            .register(registry);
            
        this.apiResponseTimer = Timer.builder("place.api.response")
            .description("API 응답 시간")
            .register(registry);
    }
    
    /**
     * 검색 메트릭 기록
     */
    public void recordSearch(String query, boolean cached) {
        searchCounter.increment();
        registry.counter("place.search.detail",
            "cached", String.valueOf(cached),
            "has_query", String.valueOf(!query.isEmpty())
        ).increment();
        
        log.debug("검색 메트릭 기록: query={}, cached={}", query, cached);
    }
    
    /**
     * 자동완성 메트릭 기록
     */
    public void recordSuggest(String query, int resultCount) {
        suggestCounter.increment();
        registry.counter("place.suggest.detail",
            "query_length", String.valueOf(query.length()),
            "has_results", String.valueOf(resultCount > 0)
        ).increment();
    }
    
    /**
     * 상세 조회 메트릭 기록
     */
    public void recordDetail(String placeId, boolean cached) {
        detailCounter.increment();
        registry.counter("place.detail.view",
            "cached", String.valueOf(cached)
        ).increment();
    }
    
    /**
     * API 호출 메트릭 기록
     */
    public void recordApiCall(String api, boolean success, long durationMs) {
        apiResponseTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        registry.timer("place.api.call",
            "api", api,
            "success", String.valueOf(success)
        ).record(Duration.ofMillis(durationMs));
        
        if (!success) {
            apiErrorCounter.increment();
        }
    }
    
    /**
     * 캐시 히트율 기록
     */
    public void recordCacheHit(String cacheType, boolean hit) {
        registry.counter("place.cache.hit",
            "type", cacheType,
            "hit", String.valueOf(hit)
        ).increment();
    }
    
    /**
     * Rate Limit 발생 기록
     */
    public void recordRateLimit(String clientType) {
        registry.counter("place.rate.limit",
            "client_type", clientType
        ).increment();
    }
}