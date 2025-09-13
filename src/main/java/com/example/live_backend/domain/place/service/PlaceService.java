package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.domain.place.exception.PlaceException;
import com.example.live_backend.global.page.PageTemplate;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
    
    private final KakaoLocalFeign kakaoLocalFeign;
    
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>() {{
        put("LEI", "AT4");
        put("PSY", "HP8");
        put("WEL", "HP8");
        put("CSC", "PO3");
    }};
    
    private static final Map<String, String> CATEGORY_LABELS = new HashMap<>() {{
        put("LEI", "여가시설");
        put("PSY", "정신건강의학과");
        put("WEL", "복지시설");
        put("CSC", "상담센터");
    }};
    
    @Cacheable(value = "placeSearch", key = "#request.query + '_' + T(Math).round(#request.lat * 1000) + '_' + T(Math).round(#request.lng * 1000) + '_' + #request.radius + '_' + #request.page + '_' + #request.size")
    @CircuitBreaker(name = "kakaoLocal", fallbackMethod = "searchByKeywordFallback")
    @Retry(name = "kakaoLocal")
    public PageTemplate<PlaceItem> searchByKeyword(SearchRequest request) {
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
            throw PlaceException.searchFailed(request.getQuery());
        }
    }
    
    @Cacheable(value = "suggest", key = "#request.query.toLowerCase() + '_' + (#request.lat != null ? T(Math).round(#request.lat * 1000) : 0) + '_' + (#request.lng != null ? T(Math).round(#request.lng * 1000) : 0) + '_' + #request.limit + '_' + (#request.category != null ? #request.category : '')")
    @CircuitBreaker(name = "kakaoLocal", fallbackMethod = "suggestFallback")
    @Retry(name = "kakaoLocal")
    public SuggestResponse suggest(SuggestRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Getting suggestions for: {}", request.getQuery());
        
        try {

            String query = request.getQuery().trim();
            Double lng = request.hasLocation() ? request.getLng() : 126.9780;
            Double lat = request.hasLocation() ? request.getLat() : 37.5665;
            int radius = request.hasLocation() ? 5000 : 20000;

            KakaoKeywordResponse response = kakaoLocalFeign.searchByKeyword(
                query,
                lng,
                lat,
                radius,
                1,
                request.getLimit() * 2,
                request.hasLocation() ? "distance" : "accuracy"
            );
            
            // 결과를 SuggestItem으로 변환 및 필터링
            List<SuggestItem> suggestions = response.getDocuments().stream()
                .filter(place -> isMatchingQuery(place, query))
                .filter(place -> !request.hasCategory() || isMatchingCategory(place, request.getCategory()))
                .limit(request.getLimit())
                .map(place -> {
                    Integer distance = null;
                    if (request.hasLocation()) {
                        distance = calculateDistance(
                            request.getLat(), request.getLng(),
                            Double.parseDouble(place.getY()), Double.parseDouble(place.getX())
                        );
                    }
                    return SuggestItem.from(convertToPlaceItem(place), query, distance);
                })
                .toList();
            
            return SuggestResponse.of(query, suggestions, request.hasLocation(), startTime);
            
        } catch (Exception e) {
            log.error("Failed to get suggestions for: {}", request.getQuery(), e);
            return SuggestResponse.empty(request.getQuery());
        }
    }

    private boolean isMatchingQuery(KakaoKeywordResponse.KakaoPlace place, String query) {
        String lowerQuery = query.toLowerCase();
        String placeName = place.getPlaceName().toLowerCase();
        String addressName = place.getAddressName() != null ? place.getAddressName().toLowerCase() : "";
        String roadAddress = place.getRoadAddressName() != null ? place.getRoadAddressName().toLowerCase() : "";
        
        return placeName.startsWith(lowerQuery) || placeName.contains(lowerQuery) ||
               addressName.contains(lowerQuery) || roadAddress.contains(lowerQuery);
    }
    

    private boolean isMatchingCategory(KakaoKeywordResponse.KakaoPlace place, String categoryFilter) {
        String kakaoCategory = CATEGORY_MAPPING.get(categoryFilter);
        if (kakaoCategory == null) return true;
        
        if ("PSY".equals(categoryFilter)) {
            return place.getCategoryName() != null && 
                   (place.getCategoryName().contains("정신") || 
                    place.getCategoryName().contains("신경정신"));
        }
        
        return kakaoCategory.equals(place.getCategoryGroupCode());
    }
    
    private Integer calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return (int) Math.round(R * c);
    }
    
    @Cacheable(value = "placeNearby", key = "#request.category + '_' + T(Math).round(#request.lat * 1000) + '_' + T(Math).round(#request.lng * 1000) + '_' + #request.radius + '_' + #request.page + '_' + #request.size")
    @CircuitBreaker(name = "kakaoLocal", fallbackMethod = "searchByCategoryFallback")
    @Retry(name = "kakaoLocal")
    public PageTemplate<PlaceItem> searchByCategory(NearbyRequest request) {
        
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
            
            if ("PSY".equals(request.getCategory())) {
                response = filterPsychiatryOnly(response);
            }
            
            return convertToPlaceSearchResult(response, request.getPage(), request.getSize(), request.getCategory());
        } catch (PlaceException e) {
            throw e;
        } catch (Exception e) {
            throw PlaceException.kakaoApiError("카테고리 검색 실패");
        }
    }
    
    @Cacheable(value = "placeDetail", key = "#placeId")
    public PlaceDetail getPlaceDetail(String placeId) {
        
        if (!placeId.startsWith("kakao:")) {
            throw PlaceException.invalidPlaceId(placeId);
        }
        
        String kakaoId = placeId.substring(6);
        
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
                throw PlaceException.placeNotFound(placeId);
            }
            
            return convertToPlaceDetail(response.getDocuments().get(0));
        } catch (PlaceException e) {
            throw e;
        } catch (Exception e) {
            throw PlaceException.kakaoApiError("장소 상세 조회 실패");
        }
    }
    
    private PageTemplate<PlaceItem> convertToPlaceSearchResult(KakaoKeywordResponse response, int page, int size) {
        List<PlaceItem> items = response.getDocuments().stream()
            .map(this::convertToPlaceItem)
			.toList();
        
        return createSimplePageTemplate(items, page, size, !response.getMeta().isEnd());
    }
    
    private PageTemplate<PlaceItem> convertToPlaceSearchResult(KakaoCategoryResponse response, int page, int size, String categoryCode) {
        List<PlaceItem> items = response.getDocuments().stream()
            .map(doc -> convertToPlaceItem(doc, categoryCode))
            .toList();
        
        return createSimplePageTemplate(items, page, size, !response.getMeta().isEnd());
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
            .hours(new ArrayList<>())
            .intro(null)
            .photos(new ArrayList<>())
            .source("kakao")
            .build();
    }
    
    private KakaoCategoryResponse filterPsychiatryOnly(KakaoCategoryResponse response) {
        List<KakaoKeywordResponse.KakaoPlace> filtered = response.getDocuments().stream()
            .filter(place -> place.getCategoryName() != null && 
                           (place.getCategoryName().contains("정신") || 
                            place.getCategoryName().contains("신경정신")))
            .toList();
        return new KakaoCategoryResponse(filtered, response.getMeta());
    }

    
    private PageTemplate<PlaceItem> createSimplePageTemplate(List<PlaceItem> items, int page, int size, boolean hasNext) {
        long estimatedTotal = hasNext ? (long) (page + 1) * size + 1 : (long) page * size;
        int totalPages = hasNext ? page + 1 : page;
        
        return new PageTemplate<>(
            estimatedTotal,
            totalPages,
            page,
            size,
            hasNext,
            items != null ? items : List.of()
        );
    }
    
    public SuggestResponse suggestFallback(SuggestRequest request, Exception ex) {
        return SuggestResponse.empty(request.getQuery());
    }
}