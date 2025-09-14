package com.example.live_backend.domain.place.metrics;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Component
@Getter
public class PlaceMetrics {

    private final Map<String, ApiMetric> apiMetrics = new ConcurrentHashMap<>();
    
    @Getter
    public static class ApiMetric {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        
        public void record(boolean success, long durationMs) {
            callCount.incrementAndGet();
            totalTime.addAndGet(durationMs);
            
            if (!success) {
                errorCount.incrementAndGet();
            }

            updateMaxTime(durationMs);
            updateMinTime(durationMs);
        }
        
        private void updateMaxTime(long durationMs) {
            long currentMax = maxTime.get();
            if (durationMs > currentMax) {
                maxTime.compareAndSet(currentMax, durationMs);
            }
        }
        
        private void updateMinTime(long durationMs) {
            long currentMin = minTime.get();
            if (durationMs < currentMin) {
                minTime.compareAndSet(currentMin, durationMs);
            }
        }
        
        public double getAverageTime() {
            long count = callCount.get();
            return count > 0 ? (double) totalTime.get() / count : 0;
        }
        
        public double getSuccessRate() {
            long count = callCount.get();
            return count > 0 ? (double) (count - errorCount.get()) / count * 100 : 0;
        }
    }

    public void recordApiCall(String api, boolean success, long durationMs) {
        ApiMetric metric = apiMetrics.computeIfAbsent(api, k -> new ApiMetric());
        metric.record(success, durationMs);

        if (durationMs > 3000) {
            log.warn("[SLOW API] {} 응답시간: {}ms", api, durationMs);
        }

        if (!success) {
            log.error("[API ERROR] {} 실패", api);
        }
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        

        long totalCalls = apiMetrics.values().stream()
            .mapToLong(m -> m.getCallCount().get())
            .sum();

        long totalErrors = apiMetrics.values().stream()
            .mapToLong(m -> m.getErrorCount().get())
            .sum();

        double avgResponseTime = apiMetrics.values().stream()
            .mapToDouble(ApiMetric::getAverageTime)
            .average()
            .orElse(0.0);
        
        stats.put("totalCalls", totalCalls);
        stats.put("totalErrors", totalErrors);
        stats.put("overallSuccessRate", totalCalls > 0 ? (double)(totalCalls - totalErrors) / totalCalls * 100 : 0);
        stats.put("avgResponseTime", avgResponseTime);

        Map<String, Map<String, Object>> apiStats = new ConcurrentHashMap<>();
        apiMetrics.forEach((api, metric) -> {
            Map<String, Object> metricMap = new ConcurrentHashMap<>();
            metricMap.put("callCount", metric.getCallCount().get());
            metricMap.put("avgTime", String.format("%.2f", metric.getAverageTime()));
            metricMap.put("maxTime", metric.getMaxTime().get());
            metricMap.put("minTime", metric.getMinTime().get() == Long.MAX_VALUE ? 0 : metric.getMinTime().get());
            metricMap.put("successRate", String.format("%.2f%%", metric.getSuccessRate()));
            metricMap.put("errorCount", metric.getErrorCount().get());
            apiStats.put(api, metricMap);
        });
        stats.put("apiDetails", apiStats);
        
        return stats;
    }

}