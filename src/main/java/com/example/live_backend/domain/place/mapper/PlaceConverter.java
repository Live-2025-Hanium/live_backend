package com.example.live_backend.domain.place.mapper;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.infra.kakao.feign.dto.KakaoCategoryResponse;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class PlaceConverter {
    
    private final PlaceCategoryMapper categoryMapper;
    

    public PlaceItem toPlaceItem(KakaoKeywordResponse.KakaoPlace kakaoPlace) {
        return PlaceItem.builder()
            .id(formatKakaoId(kakaoPlace.getId()))
            .name(kakaoPlace.getPlaceName())
            .category(buildCategory(kakaoPlace.getCategoryGroupCode(), kakaoPlace.getCategoryGroupName()))
            .address(buildAddress(kakaoPlace))
            .location(buildLocation(kakaoPlace))
            .phone(kakaoPlace.getPhone())
            .thumbnailUrl(null)
            .source("kakao")
            .build();
    }

    public PlaceItem toPlaceItem(KakaoKeywordResponse.KakaoPlace kakaoPlace, String categoryCode) {
        return PlaceItem.builder()
            .id(formatKakaoId(kakaoPlace.getId()))
            .name(kakaoPlace.getPlaceName())
            .category(buildCategory(categoryCode, categoryMapper.getCategoryLabel(categoryCode)))
            .address(buildAddress(kakaoPlace))
            .location(buildLocation(kakaoPlace))
            .phone(kakaoPlace.getPhone())
            .thumbnailUrl(null)
            .source("kakao")
            .build();
    }

    public PlaceDetail toPlaceDetail(KakaoKeywordResponse.KakaoPlace kakaoPlace) {
        return PlaceDetail.builder()
            .id(formatKakaoId(kakaoPlace.getId()))
            .name(kakaoPlace.getPlaceName())
            .category(buildCategory(kakaoPlace.getCategoryGroupCode(), kakaoPlace.getCategoryGroupName()))
            .address(buildAddress(kakaoPlace))
            .location(buildLocation(kakaoPlace))
            .phone(kakaoPlace.getPhone())
            .hours(new ArrayList<>())
            .intro(null)
            .photos(new ArrayList<>())
            .source("kakao")
            .build();
    }

    public PageTemplate<PlaceItem> toPageTemplate(KakaoKeywordResponse response, int page, int size) {
        List<PlaceItem> items = response.getDocuments().stream()
            .map(this::toPlaceItem)
            .toList();
        
        return createPageTemplate(items, page, size, !response.getMeta().isEnd());
    }

    public PageTemplate<PlaceItem> toPageTemplate(KakaoCategoryResponse response, int page, int size, String categoryCode) {
        List<PlaceItem> items = response.getDocuments().stream()
            .map(doc -> toPlaceItem(doc, categoryCode))
            .toList();
        
        return createPageTemplate(items, page, size, !response.getMeta().isEnd());
    }
    
    private PageTemplate<PlaceItem> createPageTemplate(List<PlaceItem> items, int page, int size, boolean hasNext) {
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
    
    private String formatKakaoId(String id) {
        return "kakao:" + id;
    }
    
    private Category buildCategory(String code, String label) {
        return Category.builder()
            .code(code)
            .label(label)
            .build();
    }
    
    private Address buildAddress(KakaoKeywordResponse.KakaoPlace place) {
        return Address.builder()
            .road(place.getRoadAddressName())
            .lot(place.getAddressName())
            .build();
    }
    
    private Location buildLocation(KakaoKeywordResponse.KakaoPlace place) {
        return Location.builder()
            .lat(parseCoordinate(place.getY()))
            .lng(parseCoordinate(place.getX()))
            .build();
    }
    
    private Double parseCoordinate(String coordinate) {
        return Double.parseDouble(coordinate);
    }
}