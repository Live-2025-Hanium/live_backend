package com.example.live_backend.domain.analysis.dto;

import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DailyCompletedMissionsResponseDto {

    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private List<CompletedMission> completedMissions;

    @Getter
    @Builder
    public static class CompletedMission {
        private Long userMissionId;
        private String missionTitle;
        private LocalDateTime completedAt;
    }

    public static DailyCompletedMissionsResponseDto from(LocalDate date, List<CloverMissionRecord> completed) {
        List<CompletedMission> CompletedMissions = completed.stream()
                .map(r -> CompletedMission.builder()
                        .userMissionId(r.getMissionId())
                        .missionTitle(r.getMissionTitle())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();

        return DailyCompletedMissionsResponseDto.builder()
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .completedMissions(CompletedMissions)
                .build();
    }
}
