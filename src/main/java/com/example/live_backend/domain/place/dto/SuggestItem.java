package com.example.live_backend.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자동완성 항목")
public class SuggestItem {
    
    @Schema(description = "장소 ID", example = "kakao:123456789")
    private String id;
    
    @Schema(description = "장소명", example = "스타벅스 강남역점")
    private String name;
    
    @Schema(description = "카테고리", example = "카페")
    private String category;
    
    @Schema(description = "주소 (도로명)", example = "서울 강남구 강남대로 390")
    private String address;
    
    @Schema(description = "거리 (미터, 위치 정보 제공시)", example = "125")
    private Integer distance;
    
    @Schema(description = "매칭 타입 (NAME: 이름 매칭, ADDRESS: 주소 매칭)", example = "NAME")
    private MatchType matchType;
    
    @Schema(description = "강조 표시용 매칭 위치 (시작 인덱스)", example = "0")
    private Integer highlightStart;
    
    @Schema(description = "강조 표시용 매칭 위치 (끝 인덱스)", example = "3")
    private Integer highlightEnd;
    
    public enum MatchType {
        NAME,
        ADDRESS,
        CATEGORY
    }

    public static SuggestItem from(PlaceItem place, String query, Integer distance) {
        String name = place.getName();
        String address = place.getAddress() != null && place.getAddress().getRoad() != null 
            ? place.getAddress().getRoad() 
            : place.getAddress() != null ? place.getAddress().getLot() : "";

        MatchType matchType;
        int highlightStart = -1;
        int highlightEnd = -1;
        
        String lowerQuery = query.toLowerCase();
        String lowerName = name.toLowerCase();
        
        if (lowerName.contains(lowerQuery)) {
            matchType = MatchType.NAME;
            highlightStart = lowerName.indexOf(lowerQuery);
            highlightEnd = highlightStart + query.length();
        } else if (address.toLowerCase().contains(lowerQuery)) {
            matchType = MatchType.ADDRESS;
            highlightStart = address.toLowerCase().indexOf(lowerQuery);
            highlightEnd = highlightStart + query.length();
        } else {
            matchType = MatchType.CATEGORY;
        }
        
        return SuggestItem.builder()
            .id(place.getId())
            .name(name)
            .category(place.getCategory() != null ? place.getCategory().getLabel() : null)
            .address(address)
            .distance(distance)
            .matchType(matchType)
            .highlightStart(highlightStart)
            .highlightEnd(highlightEnd)
            .build();
    }
}