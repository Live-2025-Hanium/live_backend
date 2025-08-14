package com.example.live_backend.domain.mission.dto;

import com.example.live_backend.domain.mission.Enum.MissionCategory;
import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.Enum.CloverMissionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloverMissionResponseDto {

    @Schema(description = "미션 기록 ID", example = "10")
    private Long userMissionId;

    @Schema(description = "미션 타입", example = "TIMER / DISTANCE / PHOTO / VISIT")
    private String cloverType;

    @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
    private String missionTitle;

    @Schema(description = "미션 설명", example = "오늘 이야기를 나누지 않은 동료에게 인사를 해보세요.")
    private String description;

    @Schema(description = "미션 수행 상태", example = "ASSIGNED / STARTED / PAUSED / COMPLETED")
    private CloverMissionStatus cloverMissionStatus;

    @Schema(description = "미션 카테고리", example = "EASY")
    private MissionCategory missionCategory;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDifficulty missionDifficulty;

    @Schema(description = "타이머 미션 남은 시간", example = "10:30")
    private String remainingTime;

    @Schema(description = "거리 미션 남은 거리", example = "500")
    private Integer remainingDistance;

    @Schema(description = "방문 미션 주소", example = "주소정보")
    private String targetAddress;

    @Schema(description = "일러스트레이션 주소", example = "S3 URL")
    private String illustrationUrl;
}
