package com.example.live_backend.domain.analysis.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyParticipationResponseDto {

    private long totalAssigned;
    private long totalCompleted;
    private double completionRate; // 소수점 둘째자리에서 반올림

    public static MonthlyParticipationResponseDto from(Long assigned, Long completed, double rate) {
        return MonthlyParticipationResponseDto.builder()
                .totalAssigned(assigned)
                .totalCompleted(completed)
                .completionRate(rate)
                .build();
    }
}
