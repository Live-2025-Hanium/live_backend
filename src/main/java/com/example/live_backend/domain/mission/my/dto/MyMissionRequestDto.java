package com.example.live_backend.domain.mission.my.dto;

import com.example.live_backend.domain.mission.my.Enum.RepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class MyMissionRequestDto {

    @Schema(description = "미션 제목", example = "가족들에게 안부인사하기")
    private String missionTitle;

    @Schema(description = "미션 전체 기간 중 시작날짜", example = "2025-11-13")
    private LocalDate startDate;

    @Schema(description = "미션 전체 기간 중 종료날짜", example = "2025-11-20")
    private LocalDate endDate;

    @Schema(description = "미션 수행 시간", example = "09:00")
    private LocalTime scheduledTime;

    @Schema(description = "미션 반복 타입", example = "EVERYDAY, WEEKDAY, WEEKEND")
    private RepeatType repeatType;
}