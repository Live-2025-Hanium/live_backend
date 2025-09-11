package com.example.live_backend.domain.places.service;

import com.example.live_backend.domain.places.dto.*;
import com.example.live_backend.domain.places.dto.request.NearbyRequest;
import com.example.live_backend.domain.places.dto.request.SearchRequest;
import com.example.live_backend.domain.places.exception.PlaceException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoCategoryResponse;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
    
    private final KakaoLocalFeign kakaoLocalFeign;
    
    // 카테고리 매핑 (내부 코드 -> 카카오 카테고리 그룹 코드)
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>() {{
        put("LEI", "AT4");  // 관광명소
        put("PSY", "HP8");  // 병원
        put("WEL", "HP8");  // 병원 (복지시설도 병원 카테고리 사용)
        put("CSC", "PO3");  // 공공기관
    }};
    
    // 카테고리 라벨
    private static final Map<String, String> CATEGORY_LABELS = new HashMap<>() {{
        put("LEI", "여가시설");
        put("PSY", "정신건강의학과");
        put("WEL", "복지시설");
        put("CSC", "상담센터");
    }};
    
    @Cacheable(value = "placeSearch", key = "#request.query + '_' + T(Math).round(#request.lat * 1000) + '_' + T(Math).round(#request.lng * 1000) + '_' + #request.radius + '_' + #request.page + '_' + #request.size")
    @CircuitBreaker(name = "kakaoLocal", fallbackMethod = "searchByKeywordFallback")
    @Retry(name = "kakaoLocal")
    public PlaceSearchResult searchByKeyword(SearchRequest request) {
        log.info("Searching places by keyword: {}", request.getQuery());
        
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
            
            return convertToPlaceSearchResult(response, request.getPage(), request.getSize());
        } catch (Exception e) {
            log.error("Failed to search places by keyword: {}", request.getQuery(), e);
            throw PlaceException.searchFailed(request.getQuery());
        }
    }
    
    @Cacheable(value = "placeNearby", key = "#request.category + '_' + T(Math).round(#request.lat * 1000) + '_' + T(Math).round(#request.lng * 1000) + '_' + #request.radius + '_' + #request.page + '_' + #request.size")
    @CircuitBreaker(name = "kakaoLocal", fallbackMethod = "searchByCategoryFallback")
    @Retry(name = "kakaoLocal")
    public PlaceSearchResult searchByCategory(NearbyRequest request) {
        log.info("Searching places by category: {}", request.getCategory());
        
        String kakaoCategory = CATEGORY_MAPPING.get(request.getCategory());
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
            
            // 카테고리별 필터링 (PSY의 경우 정신과만)
            if ("PSY".equals(request.getCategory())) {
                response = filterPsychiatryOnly(response);
            }
            
            return convertToPlaceSearchResult(response, request.getPage(), request.getSize(), request.getCategory());
        } catch (PlaceException e) {
            throw e;  // PlaceException은 그대로 전파
        } catch (Exception e) {
            log.error("Failed to search places by category: {}", request.getCategory(), e);
            throw PlaceException.kakaoApiError("카테고리 검색 실패");
        }
    }
    
    @Cacheable(value = "placeDetail", key = "#placeId")
    public PlaceDetail getPlaceDetail(String placeId) {
        log.info("Getting place detail for: {}", placeId);
        
        // placeId 형식: "kakao:123456789"
        if (!placeId.startsWith("kakao:")) {
            throw PlaceException.invalidPlaceId(placeId);
        }
        
        String kakaoId = placeId.substring(6);
        
        // 카카오 API는 상세 조회를 별도로 제공하지 않으므로,
        // ID로 검색하여 첫 번째 결과를 반환
        try {
            KakaoKeywordResponse response = kakaoLocalFeign.searchByKeyword(
                kakaoId,
                127.0,  // 기본 좌표 (전국 검색)
                37.5,
                20000,  // 최대 범위
                1,
                1,
                null
            );
            
            if (response.getDocuments().isEmpty()) {
                throw PlaceException.placeNotFound(placeId);
            }
            
            return convertToPlaceDetail(response.getDocuments().get(0));
        } catch (PlaceException e) {
            throw e;  // PlaceException은 그대로 전파
        } catch (Exception e) {
            log.error("Failed to get place detail for: {}", placeId, e);
            throw PlaceException.kakaoApiError("장소 상세 조회 실패");
        }
    }
    
    private PlaceSearchResult convertToPlaceSearchResult(KakaoKeywordResponse response, int page, int size) {
        List<PlaceItem> items = response.getDocuments().stream()
            .map(this::convertToPlaceItem)
            .collect(Collectors.toList());
        
        PlaceSearchResult.PlacePage pageInfo = PlaceSearchResult.PlacePage.builder()
            .number(page)
            .size(size)
            .hasNext(!response.getMeta().isEnd())
            .build();
        
        return PlaceSearchResult.builder()
            .items(items)
            .page(pageInfo)
            .build();
    }
    
    private PlaceSearchResult convertToPlaceSearchResult(KakaoCategoryResponse response, int page, int size, String categoryCode) {
        List<PlaceItem> items = response.getDocuments().stream()
            .map(doc -> convertToPlaceItem(doc, categoryCode))
            .collect(Collectors.toList());
        
        PlaceSearchResult.PlacePage pageInfo = PlaceSearchResult.PlacePage.builder()
            .number(page)
            .size(size)
            .hasNext(!response.getMeta().isEnd())
            .build();
        
        return PlaceSearchResult.builder()
            .items(items)
            .page(pageInfo)
            .build();
    }
    
    private PlaceItem convertToPlaceItem(KakaoKeywordResponse.KakaoPlace kakaoPlace) {
        return PlaceItem.builder()
            .id("kakao:" + kakaoPlace.getId())
            .name(kakaoPlace.getPlaceName())
            .category(Category.builder()
                .code(kakaoPlace.getCategoryGroupCode())
                .label(kakaoPlace.getCategoryGroupName())
                .build())
            .address(Address.builder()
                .road(kakaoPlace.getRoadAddressName())
                .lot(kakaoPlace.getAddressName())
                .build())
            .location(Location.builder()
                .lat(Double.parseDouble(kakaoPlace.getY()))
                .lng(Double.parseDouble(kakaoPlace.getX()))
                .build())
            .phone(kakaoPlace.getPhone())
            .thumbnailUrl(null)
            .source("kakao")
            .build();
    }
    
    private PlaceItem convertToPlaceItem(KakaoKeywordResponse.KakaoPlace kakaoPlace, String categoryCode) {
        return PlaceItem.builder()
            .id("kakao:" + kakaoPlace.getId())
            .name(kakaoPlace.getPlaceName())
            .category(Category.builder()
                .code(categoryCode)
                .label(CATEGORY_LABELS.get(categoryCode))
                .build())
            .address(Address.builder()
                .road(kakaoPlace.getRoadAddressName())
                .lot(kakaoPlace.getAddressName())
                .build())
            .location(Location.builder()
                .lat(Double.parseDouble(kakaoPlace.getY()))
                .lng(Double.parseDouble(kakaoPlace.getX()))
                .build())
            .phone(kakaoPlace.getPhone())
            .thumbnailUrl(null)
            .source("kakao")
            .build();
    }
    
    private PlaceDetail convertToPlaceDetail(KakaoKeywordResponse.KakaoPlace kakaoPlace) {
        return PlaceDetail.builder()
            .id("kakao:" + kakaoPlace.getId())
            .name(kakaoPlace.getPlaceName())
            .category(Category.builder()
                .code(kakaoPlace.getCategoryGroupCode())
                .label(kakaoPlace.getCategoryGroupName())
                .build())
            .address(Address.builder()
                .road(kakaoPlace.getRoadAddressName())
                .lot(kakaoPlace.getAddressName())
                .build())
            .location(Location.builder()
                .lat(Double.parseDouble(kakaoPlace.getY()))
                .lng(Double.parseDouble(kakaoPlace.getX()))
                .build())
            .phone(kakaoPlace.getPhone())
            .hours(new ArrayList<>())  // 카카오 API에서 제공하지 않음
            .intro(null)  // 추후 DB에서 보정 데이터 조회
            .photos(new ArrayList<>())  // 추후 DB에서 보정 데이터 조회
            .source("kakao")
            .build();
    }
    
    private KakaoCategoryResponse filterPsychiatryOnly(KakaoCategoryResponse response) {
        List<KakaoKeywordResponse.KakaoPlace> filtered = response.getDocuments().stream()
            .filter(place -> place.getCategoryName() != null && 
                           (place.getCategoryName().contains("정신") || 
                            place.getCategoryName().contains("신경정신")))
            .collect(Collectors.toList());
        
        return new KakaoCategoryResponse(filtered, response.getMeta());
    }
    
    // Circuit Breaker Fallback 메서드들
    public PlaceSearchResult searchByKeywordFallback(SearchRequest request, Exception ex) {
        log.error("Circuit breaker opened for searchByKeyword. Returning empty result", ex);
        return PlaceSearchResult.builder()
            .items(new ArrayList<>())
            .page(PlaceSearchResult.PlacePage.builder()
                .number(request.getPage())
                .size(request.getSize())
                .hasNext(false)
                .build())
            .build();
    }
    
    public PlaceSearchResult searchByCategoryFallback(NearbyRequest request, Exception ex) {
        log.error("Circuit breaker opened for searchByCategory. Returning empty result", ex);
        return PlaceSearchResult.builder()
            .items(new ArrayList<>())
            .page(PlaceSearchResult.PlacePage.builder()
                .number(request.getPage())
                .size(request.getSize())
                .hasNext(false)
                .build())
            .build();
    }
}