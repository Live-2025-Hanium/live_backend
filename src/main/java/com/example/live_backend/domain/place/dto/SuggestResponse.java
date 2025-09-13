package com.example.live_backend.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자동완성 검색 응답")
public class SuggestResponse {
    
    @Schema(description = "검색어", example = "스타")
    private String query;
    
    @Schema(description = "자동완성 결과 목록")
    private List<SuggestItem> suggestions;
    
    @Schema(description = "결과 개수", example = "10")
    private int count;
    
    @Schema(description = "위치 기반 정렬 여부", example = "true")
    private boolean locationBased;
    
    @Schema(description = "응답 시간 (밀리초)", example = "45")
    private long responseTime;
    
    /**
     * 빈 결과 생성
     */
    public static SuggestResponse empty(String query) {
        return SuggestResponse.builder()
            .query(query)
            .suggestions(List.of())
            .count(0)
            .locationBased(false)
            .responseTime(0)
            .build();
    }

    public static SuggestResponse of(String query, List<SuggestItem> suggestions, boolean locationBased, long startTime) {
        return SuggestResponse.builder()
            .query(query)
            .suggestions(suggestions)
            .count(suggestions.size())
            .locationBased(locationBased)
            .responseTime(System.currentTimeMillis() - startTime)
            .build();
    }
}