package com.example.live_backend.domain.analysis.dto;

import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DailyCompletedMyMissionsResponseDto {

    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private List<CompletedMyMission> completedMissions;

    @Getter
    @Builder
    public static class CompletedMyMission {
        private Long missionId;
        private Long missionRecordId;
        private String missionTitle;
        private LocalDateTime completedAt;
    }

    public static DailyCompletedMyMissionsResponseDto from(LocalDate date, List<MyMissionRecord> completed) {
        List<CompletedMyMission> completedMyMissions = completed.stream()
                .map(r -> CompletedMyMission.builder()
                        .missionRecordId(r.getId())
                        .missionId(r.getMyMission().getId())
                        .missionTitle(r.getMyMission().getTitle())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();

        return DailyCompletedMyMissionsResponseDto.builder()
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .completedMissions(completedMyMissions)
                .build();
    }
}
