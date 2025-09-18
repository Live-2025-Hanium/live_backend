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
        retryFor = {FeignException.FeignServerException.class},
        noRetryFor = {FeignException.FeignClientException.class, CustomException.class},
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
                127.0,
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
            
        } catch (FeignException.FeignServerException e) {
            log.error("카카오 API 서버 오류, 폴백 반환: {}", e.getMessage());
            return createFallbackDetail(placeId);
        } catch (FeignException.FeignClientException e) {
            if (e.status() == 404) {
                log.info("장소를 찾을 수 없음: {}", placeId);
                throw new CustomException(ErrorCode.PLACE_NOT_FOUND, "장소를 찾을 수 없습니다: " + placeId);
            }
            log.warn("카카오 API 클라이언트 오류, 폴백 반환: {}", e.getMessage());
            return createFallbackDetail(placeId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류, 폴백 반환: {}", e.getMessage(), e);
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