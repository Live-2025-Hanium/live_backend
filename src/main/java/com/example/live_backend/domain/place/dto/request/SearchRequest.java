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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "키워드 검색 요청")
public class SearchRequest {
    
    @NotBlank(message = "검색어는 필수입니다")
    @Size(min = 2, max = 50, message = "검색어는 2~50자 사이여야 합니다")
    @Schema(description = "검색 키워드", example = "정신과")
    private String query;
    
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    @Schema(description = "위도", example = "37.56")
    private double lat;
    
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
    @Schema(description = "경도", example = "127.02")
    private double lng;
    
    @Min(value = 200, message = "반경은 200m 이상이어야 합니다")
    @Max(value = 3000, message = "반경은 3000m 이하여야 합니다")
    @Schema(description = "검색 반경 (미터)", example = "1000")
    private int radius;
    
    @Min(value = 1, message = "페이지는 1 이상이어야 합니다")
    @Schema(description = "페이지 번호", example = "1")
    private int page = 1;
    
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 15, message = "페이지 크기는 15 이하여야 합니다")
    @Schema(description = "페이지 크기", example = "15")
    private int size = 15;
    
    @Schema(description = "정렬 기준 (distance|accuracy)", example = "distance")
    private String sort = "distance";
}