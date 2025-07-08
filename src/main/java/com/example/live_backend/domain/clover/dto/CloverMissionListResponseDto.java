package com.example.live_backend.domain.clover.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloverMissionListResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "조회 날짜", example = "2024-01-15T10:30:00")
    private LocalDateTime searchDate;

    @Schema(description = "오늘의 미션 목록")
    private List<TodayMissionDto> missions;

    @Getter
    @Builder
    @Schema(description = "개별 미션 정보")
    public static class TodayMissionDto {

        @Schema(description = "사용자 미션 ID", example = "10")
        private Long missionId;

        @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
        private String title;
    }

}
