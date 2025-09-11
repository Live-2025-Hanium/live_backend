package com.example.live_backend.domain.places.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "카테고리 기반 주변 검색 요청")
public class NearbyRequest {
    
    @NotBlank(message = "카테고리는 필수입니다")
    @Pattern(regexp = "LEI|PSY|WEL|CSC", message = "카테고리는 LEI, PSY, WEL, CSC 중 하나여야 합니다")
    @Schema(description = "카테고리 코드 (LEI|PSY|WEL|CSC)", example = "PSY")
    private String category;
    
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
}