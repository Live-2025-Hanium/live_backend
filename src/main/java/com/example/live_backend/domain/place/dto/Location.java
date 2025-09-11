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
@Schema(description = "위치 좌표")
public class Location {
    
    @Schema(description = "위도", example = "37.56")
    private double lat;
    
    @Schema(description = "경도", example = "127.02")
    private double lng;
}