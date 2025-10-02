package com.example.live_backend.domain.analysis.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyMyMissionParticipationResponseDto {

    private long totalAssigned;
    private long totalCompleted;
    private double completionRate; // 소수점 둘째자리에서 반올림

    public static MonthlyMyMissionParticipationResponseDto from(Long assigned, Long completed, double rate) {
        return MonthlyMyMissionParticipationResponseDto.builder()
                .totalAssigned(assigned)
                .totalCompleted(completed)
                .completionRate(rate)
                .build();
    }
}
