package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.PlaceItem;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.exception.PlaceException;
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
        value = {Exception.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 500, multiplier = 2)
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
        } catch (Exception e) {
            log.warn("키워드 검색 실패 (재시도 2회 수행): {}", request.getQuery(), e);
            return createEmptyResult(request.getPage(), request.getSize());
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
        value = {Exception.class},
        exclude = {PlaceException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public PageTemplate<PlaceItem> searchByCategory(NearbyRequest request) {
        log.info("카테고리로 주변 장소 검색 중: {}", request.getCategory());
        
        String kakaoCategory = categoryMapper.toKakaoCategory(request.getCategory());
        if (kakaoCategory == null) {
            throw PlaceException.invalidCategory(request.getCategory());
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
        } catch (PlaceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("카테고리 검색 실패 (재시도 2회 수행): {}", request.getCategory(), e);
            return createEmptyResult(request.getPage(), request.getSize());
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