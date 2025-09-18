package com.example.live_backend.domain.place.aop;

import com.example.live_backend.domain.place.metrics.PlaceMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PlaceApiMonitoringAspect {

	private final PlaceMetrics placeMetrics;

	@Around("execution(public * com.example.live_backend.domain.place.controller.PlaceController.*(..))")
	public Object monitorPlaceApi(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		long startTime = System.currentTimeMillis();

		log.debug("[API 시작] {}", methodName);

		try {

			Object result = joinPoint.proceed();

			long duration = System.currentTimeMillis() - startTime;
			placeMetrics.recordApiCall(methodName, true, duration);

			log.debug("[API 완료] {} - {}ms", methodName, duration);

			return result;

		} catch (Exception e) {

			long duration = System.currentTimeMillis() - startTime;
			placeMetrics.recordApiCall(methodName, false, duration);

			log.error("[API 실패] {} - {}ms, 에러: {}",
				methodName, duration, e.getMessage());

			throw e;
		}
	}
}
