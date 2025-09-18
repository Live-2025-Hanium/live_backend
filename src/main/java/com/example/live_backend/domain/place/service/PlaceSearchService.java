package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.PlaceItem;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.domain.place.mapper.PlaceCategoryMapper;
import com.example.live_backend.domain.place.mapper.PlaceConverter;
import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoCategoryResponse;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import feign.FeignException;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchService {
    
    private final KakaoLocalFeign kakaoLocalFeign;
    private final PlaceCategoryMapper categoryMapper;
    private final PlaceConverter placeConverter;
    

    @Cacheable(
        value = "placeSearch",
        key = "#request.query + '_' + (#request.lat != null ? T(Math).round(#request.lat * 100) : 0) + '_' + (#request.lng != null ? T(Math).round(#request.lng * 100) : 0) + '_' + #request.radius",
        condition = "#request.page == 1"
    )
    @Retryable(
        retryFor = {FeignException.FeignServerException.class},  // 5xx 서버 오류만 재시도
        noRetryFor = {FeignException.FeignClientException.class}, // 4xx 클라이언트 오류는 재시도 안함
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public PageTemplate<PlaceItem> searchByKeyword(SearchRequest request) {
        log.info("키워드로 장소 검색 중: {}", request.getQuery());
        
        try {
            KakaoKeywordResponse response = kakaoLocalFeign.searchByKeyword(
                request.getQuery(),
                request.getLng(),
                request.getLat(),
                request.getRadius(),
                request.getPage(),
                request.getSize(),
                request.getSort()
            );

            return placeConverter.toPageTemplate(response, request.getPage(), request.getSize());
        } catch (FeignException.FeignServerException e) {
            log.error("카카오 API 서버 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 서버 오류가 발생했습니다");
        } catch (FeignException.FeignClientException e) {
            if (e.status() == 401 || e.status() == 403) {
                log.error("카카오 API 인증 오류: {}", e.getMessage());
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 인증에 실패했습니다");
            }
            log.warn("클라이언트 오류로 빈 결과 반환: {}", e.getMessage());
            return createEmptyResult(request.getPage(), request.getSize());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "장소 검색 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 카테고리로 주변 장소 검색
     */
    @Cacheable(
        value = "placeNearby",
        key = "#request.category + '_' + T(Math).round(#request.lat * 100) + '_' + T(Math).round(#request.lng * 100) + '_' + #request.radius",
        condition = "#request.page == 1"
    )
    @Retryable(
        retryFor = {FeignException.FeignServerException.class},  // 5xx 서버 오류만 재시도
        noRetryFor = {FeignException.FeignClientException.class, CustomException.class}, // 4xx와 비즈니스 오류는 재시도 안함
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public PageTemplate<PlaceItem> searchByCategory(NearbyRequest request) {
        log.info("카테고리로 주변 장소 검색 중: {}", request.getCategory());
        
        String kakaoCategory = categoryMapper.toKakaoCategory(request.getCategory());
        if (kakaoCategory == null) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY, "지원하지 않는 카테고리: " + request.getCategory());
        }
        
        try {
            KakaoCategoryResponse response = kakaoLocalFeign.searchByCategory(
                kakaoCategory,
                request.getLng(),
                request.getLat(),
                request.getRadius(),
                request.getPage(),
                request.getSize()
            );
            
            // 정신과 카테고리는 추가 필터링
            if (categoryMapper.isPsychiatryCategory(request.getCategory())) {
                response = filterPsychiatryOnly(response);
            }
            
            return placeConverter.toPageTemplate(response, request.getPage(), request.getSize(), request.getCategory());
        } catch (FeignException.FeignServerException e) {
            log.error("카카오 API 서버 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 서버 오류가 발생했습니다");
        } catch (FeignException.FeignClientException e) {
            if (e.status() == 401 || e.status() == 403) {
                log.error("카카오 API 인증 오류: {}", e.getMessage());
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 인증에 실패했습니다");
            }
            log.warn("클라이언트 오류로 빈 결과 반환: {}", e.getMessage());
            return createEmptyResult(request.getPage(), request.getSize());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "장소 검색 중 오류가 발생했습니다");
        }
    }
    
    private KakaoCategoryResponse filterPsychiatryOnly(KakaoCategoryResponse response) {
        List<KakaoKeywordResponse.KakaoPlace> filtered = response.getDocuments().stream()
            .filter(place -> place.getCategoryName() != null && 
                           (place.getCategoryName().contains("정신") || 
                            place.getCategoryName().contains("신경정신")))
            .toList();
        return new KakaoCategoryResponse(filtered, response.getMeta());
    }
    
    private PageTemplate<PlaceItem> createEmptyResult(int page, int size) {
        return new PageTemplate<>(0L, 0, page, size, false, new ArrayList<>());
    }
}