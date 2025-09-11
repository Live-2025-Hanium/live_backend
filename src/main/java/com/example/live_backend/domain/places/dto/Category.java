package com.example.live_backend.domain.places.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "카테고리 정보")
public class Category {
    
    @Schema(description = "카테고리 코드", example = "PSY")
    private String code;
    
    @Schema(description = "카테고리 라벨", example = "정신건강의학과")
    private String label;
}