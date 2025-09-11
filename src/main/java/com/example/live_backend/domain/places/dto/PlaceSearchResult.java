package com.example.live_backend.domain.places.dto;

import com.example.live_backend.global.page.PageTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장소 검색 결과")
public class PlaceSearchResult {
    
    @Schema(description = "검색된 장소 목록")
    private List<PlaceItem> items;
    
    @Schema(description = "페이지 정보")
    private PlacePage page;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "페이지 정보")
    public static class PlacePage {
        
        @Schema(description = "현재 페이지 번호", example = "1")
        private int number;
        
        @Schema(description = "페이지 크기", example = "15")
        private int size;
        
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;
    }
}