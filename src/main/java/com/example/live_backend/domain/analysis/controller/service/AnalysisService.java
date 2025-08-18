package com.example.live_backend.domain.analysis.controller.service;

import com.example.live_backend.domain.analysis.controller.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final CloverMissionRecordRepository cloverMissionRecordRepository;

    public MonthlyParticipationResponseDto getMonthlyParticipation(Long memberId, YearMonth ym) {
        LocalDate monthStartDate = ym.atDay(1);
        LocalDate monthEndDate = ym.atEndOfMonth();

        long assigned = cloverMissionRecordRepository.countAssignedInPeriod(
                memberId, monthStartDate, monthEndDate
        );

        LocalDateTime monthStartDateTime = monthStartDate.atStartOfDay();
        LocalDateTime monthEndDateTime = monthEndDate.atTime(LocalTime.MAX);

        long completed = cloverMissionRecordRepository.countCompletedInPeriod(
                memberId, CloverMissionStatus.COMPLETED, monthStartDateTime, monthEndDateTime
        );

        double rate = assigned == 0 ? 0.0 : (completed * 100.0) / assigned;

        return MonthlyParticipationResponseDto.from(ym, assigned, completed, rate);
    }

}
