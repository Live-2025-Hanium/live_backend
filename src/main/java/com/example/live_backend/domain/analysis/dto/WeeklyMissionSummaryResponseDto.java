package com.example.live_backend.domain.analysis.dto;

import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class WeeklyMissionSummaryResponseDto {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<DaySummary> weeklySummary;

    @Getter
    @Builder
    public static class DaySummary {
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private long cloverMissionCount;
    }

    public static WeeklyMissionSummaryResponseDto from(LocalDate weekStartDate, LocalDate weekEndDate, List<CloverMissionRecord> completedInWeek) {

        Map<LocalDate, Long> counts = completedInWeek.stream()
                .collect(Collectors.groupingBy(r -> r.getCompletedAt().toLocalDate(), Collectors.counting()));

        List<DaySummary> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStartDate.plusDays(i);
            days.add(DaySummary.builder()
                    .date(d)
                    .dayOfWeek(d.getDayOfWeek())
                    .cloverMissionCount(counts.getOrDefault(d, 0L).intValue())
                    .build());
        }

        return WeeklyMissionSummaryResponseDto.builder()
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .weeklySummary(days)
                .build();
    }
}
