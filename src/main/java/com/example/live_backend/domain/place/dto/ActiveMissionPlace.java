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
@Schema(description = "활성 미션 장소 정보")
public class ActiveMissionPlace {
    
    @Schema(description = "장소 ID", example = "kakao:123456789")
    private String placeId;
    
    @Schema(description = "위치 좌표")
    private Location location;
    
    @Schema(description = "장소명", example = "더마음의원")
    private String name;
}