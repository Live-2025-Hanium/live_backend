package com.example.live_backend.domain.analysis.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
public class MonthlyParticipationResponseDto {

    private int year;
    private int month;
    private long totalAssigned;
    private long totalCompleted;
    private double completionRate; // 0.0 ~ 100.0

    public static MonthlyParticipationResponseDto from(YearMonth ym, Long assigned, Long completed, double rate) {
        return MonthlyParticipationResponseDto.builder()
                .year(ym.getYear())
                .month(ym.getMonthValue())
                .totalAssigned(assigned)
                .totalCompleted(completed)
                .completionRate(rate)
                .build();
    }
}
