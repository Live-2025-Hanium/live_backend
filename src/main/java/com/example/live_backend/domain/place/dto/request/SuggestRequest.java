package com.example.live_backend.domain.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자동완성 검색 요청")
public class SuggestRequest {
    
    @NotBlank(message = "검색어는 필수입니다")
    @Size(min = 1, max = 50, message = "검색어는 1자 이상 50자 이하여야 합니다")
    @Schema(description = "검색어 (최소 1자)", example = "스타")
    private String query;
    
    @Schema(description = "현재 위치 위도", example = "37.5665")
    private Double lat;
    
    @Schema(description = "현재 위치 경도", example = "126.9780")
    private Double lng;
    
    @Min(value = 5, message = "최소 5개 이상 요청해야 합니다")
    @Max(value = 20, message = "최대 20개까지 요청 가능합니다")
    @Schema(description = "반환할 결과 개수", defaultValue = "10", minimum = "5", maximum = "20")
    @Builder.Default
    private int limit = 10;
    
    @Schema(description = "카테고리 필터 (LEI, PSY, WEL, CSC)", example = "LEI")
    private String category;

    public boolean hasLocation() {
        return lat != null && lng != null;
    }

    public boolean hasCategory() {
        return category != null && !category.isBlank();
    }
}