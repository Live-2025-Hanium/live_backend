package com.example.live_backend.domain.mission.dto;

import com.example.live_backend.domain.mission.Enum.MissionCategory;
import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.Enum.MissionStatus;
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
public class CloverMissionListResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "오늘의 미션 목록")
    private List<CloverMissionList> missions;

    @Getter
    @Builder
    @Schema(description = "개별 미션 정보")
    public static class CloverMissionList {

        @Schema(description = "미션 기록 ID", example = "10")
        private Long userMissionId;

        @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
        private String missionTitle;

        @Schema(description = "미션 수행 상태", example = "ASSIGNED / STARTED / PAUSED / COMPLETED")
        private MissionStatus missionStatus;

        @Schema(description = "미션 난이도", example = "VERY_EASY / EASY / NORMAL / HARD / VERY_HARD")
        private MissionDifficulty missionDifficulty;

        @Schema(description = "미션 카테고리", example = "RELATIONSHIP / ENVIRONMENT / HEALTH ...")
        private MissionCategory missionCategory;
    }
}
