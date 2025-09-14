package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.SuggestItem;
import com.example.live_backend.domain.place.dto.SuggestResponse;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.domain.place.mapper.PlaceCategoryMapper;
import com.example.live_backend.domain.place.mapper.PlaceConverter;
import com.example.live_backend.domain.place.util.PlaceDistanceCalculator;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSuggestService {
    
    private final KakaoLocalFeign kakaoLocalFeign;
    private final PlaceCategoryMapper categoryMapper;
    private final PlaceConverter placeConverter;
    private final PlaceDistanceCalculator distanceCalculator;
    

    @Cacheable(
        value = "suggest",
        key = "#request.query.toLowerCase().substring(0, T(Math).min(#request.query.length(), 10)) + '_' + (#request.lat != null ? T(Math).round(#request.lat * 100) : 0) + '_' + (#request.lng != null ? T(Math).round(#request.lng * 100) : 0)",
        condition = "#request.query.length() >= 2"
    )
    @Retryable(value = {Exception.class}, maxAttempts = 1)
    public SuggestResponse suggest(SuggestRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("자동완성 제안 조회 중: {}", request.getQuery());
        
        try {
            String query = request.getQuery().trim();

            Double lng = request.hasLocation() ? request.getLng() : 126.9780;
            Double lat = request.hasLocation() ? request.getLat() : 37.5665;
            int radius = request.hasLocation() ? 5000 : 20000;
            

            KakaoKeywordResponse response = kakaoLocalFeign.searchByKeyword(
                query, lng, lat, radius, 1,
                request.getLimit(),
                request.hasLocation() ? "distance" : "accuracy"
            );

            List<SuggestItem> suggestions = response.getDocuments().stream()
                .filter(place -> isMatchingQuery(place, query))
                .filter(place -> !request.hasCategory() || 
                               isMatchingCategory(place, request.getCategory()))
                .limit(request.getLimit())
                .map(place -> {
                    Integer distance = null;
                    if (request.hasLocation()) {
                        distance = distanceCalculator.calculate(
                            request.getLat(), request.getLng(),
                            Double.parseDouble(place.getY()), 
                            Double.parseDouble(place.getX())
                        );
                    }
                    return SuggestItem.from(
                        placeConverter.toPlaceItem(place), 
                        query, 
                        distance
                    );
                })
                .toList();
            
            return SuggestResponse.of(query, suggestions, request.hasLocation(), startTime);
            
        } catch (Exception e) {
            log.warn("자동완성 조회 실패: {}", request.getQuery(), e);
            return SuggestResponse.empty(request.getQuery());
        }
    }
    
    private boolean isMatchingQuery(KakaoKeywordResponse.KakaoPlace place, String query) {
        String lowerQuery = query.toLowerCase();
        String placeName = place.getPlaceName().toLowerCase();
        String addressName = place.getAddressName() != null ? 
            place.getAddressName().toLowerCase() : "";
        String roadAddress = place.getRoadAddressName() != null ? 
            place.getRoadAddressName().toLowerCase() : "";
        
        return placeName.startsWith(lowerQuery) || 
               placeName.contains(lowerQuery) ||
               addressName.contains(lowerQuery) || 
               roadAddress.contains(lowerQuery);
    }
    
    private boolean isMatchingCategory(KakaoKeywordResponse.KakaoPlace place, String categoryFilter) {
        if (!categoryMapper.isValidCategory(categoryFilter)) {
            return true;
        }
        
        if (categoryMapper.isPsychiatryCategory(categoryFilter)) {
            return place.getCategoryName() != null && 
                   (place.getCategoryName().contains("정신") || 
                    place.getCategoryName().contains("신경정신"));
        }
        
        String kakaoCategory = categoryMapper.toKakaoCategory(categoryFilter);
        return kakaoCategory.equals(place.getCategoryGroupCode());
    }
}