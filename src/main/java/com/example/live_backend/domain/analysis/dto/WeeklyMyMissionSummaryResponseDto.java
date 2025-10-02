package com.example.live_backend.domain.analysis.dto;

import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
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
public class WeeklyMyMissionSummaryResponseDto {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<DaySummary> weeklySummary;

    @Getter
    @Builder
    public static class DaySummary {
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private int myMissionCount;
    }

    public static WeeklyMyMissionSummaryResponseDto from(LocalDate weekStartDate, LocalDate weekEndDate, List<MyMissionRecord> completedInWeek) {

        Map<LocalDate, Integer> counts = completedInWeek.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCompletedAt().toLocalDate(),
                        Collectors.summingInt(e -> 1)));

        List<DaySummary> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStartDate.plusDays(i);
            days.add(DaySummary.builder()
                    .date(d)
                    .dayOfWeek(d.getDayOfWeek())
                    .myMissionCount(counts.getOrDefault(d, 0))
                    .build());
        }

        return WeeklyMyMissionSummaryResponseDto.builder()
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .weeklySummary(days)
                .build();
    }
}
