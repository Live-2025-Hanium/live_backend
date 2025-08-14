package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloverMissionStatusResponseDto {

    @Schema(description = "미션 기록 ID", example = "10")
    private Long userMissionId;

    @Schema(description = "미션 제목", example = "가족들에게 안부인사하기")
    private String missionTitle;

    @Schema(description = "미션 진행 상태", example = "STARTED")
    private CloverMissionStatus cloverMissionStatus;

    @Schema(description = "미션 할당 날짜", example = "2025-11-13")
    private LocalDate assignedDate;

    public static CloverMissionStatusResponseDto from(CloverMissionRecord cloverMissionRecord) {
        return CloverMissionStatusResponseDto.builder()
                .userMissionId(cloverMissionRecord.getMissionId())
                .missionTitle(cloverMissionRecord.getMissionTitle())
                .cloverMissionStatus(cloverMissionRecord.getCloverMissionStatus())
                .assignedDate(cloverMissionRecord.getAssignedDate())
                .build();
    }
}
