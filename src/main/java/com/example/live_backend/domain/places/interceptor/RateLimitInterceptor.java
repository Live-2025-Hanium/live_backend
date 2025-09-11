package com.example.live_backend.domain.places.interceptor;

import com.example.live_backend.domain.places.config.RateLimitConfig;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // /v1/places 경로에만 레이트 리밋 적용
        if (!path.startsWith("/v1/places")) {
            return true;
        }
        
        // 클라이언트 식별 (IP 또는 인증 토큰)
        String key = getClientKey(request);
        
        // 버킷 조회
        Bucket bucket = rateLimitConfig.resolveBucket(key);
        
        // 토큰 소비 시도
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // 남은 토큰 수를 헤더에 추가
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // 레이트 리밋 초과
            long waitForRefill = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            // 공통 응답 객체 사용
            ResponseHandler<?> errorResponse = ResponseHandler.error(ErrorCode.RATE_LIMITED);
            String errorMessage = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(errorMessage);
            
            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }
    }
    
    private String getClientKey(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출 시도
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "token:" + authHeader.substring(7, Math.min(authHeader.length(), 27)); // 토큰 앞 20자만 사용
        }
        
        // 2. IP 주소 사용
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        
        return "ip:" + clientIp;
    }
}