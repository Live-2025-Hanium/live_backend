package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.PlaceDetail;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.domain.place.mapper.PlaceConverter;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceDetailService {
    
    private final KakaoLocalFeign kakaoLocalFeign;
    private final PlaceConverter placeConverter;
    

    @Cacheable(value = "placeDetail", key = "#placeId")
    @Retryable(
        retryFor = {FeignException.FeignServerException.class},  // 5xx 서버 오류만 재시도
        noRetryFor = {FeignException.FeignClientException.class, CustomException.class}, // 4xx와 비즈니스 오류는 재시도 안함
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public PlaceDetail getPlaceDetail(String placeId) {
        log.info("장소 상세 정보 조회 중: {}", placeId);
        
        validatePlaceId(placeId);
        String kakaoId = extractKakaoId(placeId);
        
        try {
            KakaoKeywordResponse response = kakaoLocalFeign.searchByKeyword(
                kakaoId,
                127.0,  // 기본 좌표 (서울)
                37.5,
                20000,
                1,
                1,
                null
            );
            
            if (response.getDocuments().isEmpty()) {
                throw new CustomException(ErrorCode.PLACE_NOT_FOUND, "장소를 찾을 수 없습니다: " + placeId);
            }
            
            return placeConverter.toPlaceDetail(response.getDocuments().get(0));
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("장소 상세 조회 실패 (재시도 2회 수행): {}", placeId, e);
            return createFallbackDetail(placeId);
        }
    }
    
    private void validatePlaceId(String placeId) {
        if (!placeId.startsWith("kakao:")) {
            throw new CustomException(ErrorCode.INVALID_VALUE, "유효하지 않은 장소 ID: " + placeId);
        }
    }
    
    private String extractKakaoId(String placeId) {
        return placeId.substring(6);
    }
    
    private PlaceDetail createFallbackDetail(String placeId) {
        return PlaceDetail.builder()
            .id(placeId)
            .name("정보를 불러올 수 없습니다")
            .build();
    }
}