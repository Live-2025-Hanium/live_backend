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
    private List<CompletedMyMission> completedMyMissions;

    @Getter
    @Builder
    public static class CompletedMyMission {
        private Long myMissionRecordId;
        private Long myMissionId;
        private String missionTitle;
        private LocalDateTime completedAt;
    }

    public static DailyCompletedMyMissionsResponseDto from(LocalDate date, List<MyMissionRecord> completed) {
        List<CompletedMyMission> completedMyMissions = completed.stream()
                .map(r -> CompletedMyMission.builder()
                        .myMissionRecordId(r.getId())
                        .myMissionId(r.getMyMission().getId())
                        .missionTitle(r.getMyMission().getTitle())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();

        return DailyCompletedMyMissionsResponseDto.builder()
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .completedMyMissions(completedMyMissions)
                .build();
    }
}
