package com.example.live_backend.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주소 정보")
public class Address {
    
    @Schema(description = "도로명 주소", example = "서울 강동구 천호대로 1006")
    private String road;
    
    @Schema(description = "지번 주소", example = "서울 강동구 성내동 123-4")
    private String lot;
}