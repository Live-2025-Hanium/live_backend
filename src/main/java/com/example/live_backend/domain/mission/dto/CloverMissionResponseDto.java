package com.example.live_backend.domain.mission.dto;

import com.example.live_backend.domain.mission.entity.MissionDefault;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloverMissionResponseDto {

    @Schema(description = "사용자 미션 ID", example = "10")
    private Long missionId;

    @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
    private String title;

    @Schema(description = "미션 설명", example = "오늘 이야기를 나누지 않은 동료에게 인사를 해보세요.")
    private String description;

    @Schema(description = "미션 카테고리", example = "EASY")
    private MissionDefault.Category category;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDefault.Difficulty difficulty;
}
