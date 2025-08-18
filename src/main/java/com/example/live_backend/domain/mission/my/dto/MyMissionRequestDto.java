package com.example.live_backend.domain.mission.my.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MyMissionRequestDto {

    @Schema(description = "미션 제목", example = "가족들에게 안부인사하기")
    private String missionTitle;

    @Schema(description = "미션 전체 기간 중 시작날짜", example = "2025-11-13")
    private LocalDate startDate;

    @Schema(description = "미션 전체 기간 중 종료날짜", example = "2025-11-20")
    private LocalDate endDate;

    @Schema(description = "미션 수행 시간(알림)", example = "[\"09:00\", \"10:00\", \"11:00\"]")
    private List<LocalTime> scheduledTime;

    @Schema(description = "미션 수행일(반복 요일)", example = "[\"MONDAY\", \"SUNDAY\"]")
    private List<DayOfWeek> repeatDays;
}
