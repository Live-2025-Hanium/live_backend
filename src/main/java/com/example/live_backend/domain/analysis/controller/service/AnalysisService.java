package com.example.live_backend.domain.analysis.controller.service;

import com.example.live_backend.domain.analysis.controller.dto.DailyCompletedMissionsResponseDto;
import com.example.live_backend.domain.analysis.controller.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.analysis.controller.dto.WeeklyMissionSummaryResponseDto;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public WeeklyMissionSummaryResponseDto getWeeklySummary(Long memberId, LocalDate date) {
        LocalDate weekStartDate = date.with(DayOfWeek.MONDAY);
        LocalDate weekEndDate = date.with(DayOfWeek.SUNDAY);

        LocalDateTime weekStartDateTime = weekStartDate.atStartOfDay();
        LocalDateTime weekEndDateTime = weekEndDate.atTime(LocalTime.MAX);

        List<CloverMissionRecord> completedList = cloverMissionRecordRepository.findCompletedInPeriod(
                memberId, CloverMissionStatus.COMPLETED, weekStartDateTime, weekEndDateTime);

        return WeeklyMissionSummaryResponseDto.from(weekStartDate, weekEndDate, completedList);
    }

    public DailyCompletedMissionsResponseDto getDailyCompleted(Long memberId, LocalDate date) {
        List<CloverMissionRecord> completed = cloverMissionRecordRepository.findCompletedOnDate(
                memberId, CloverMissionStatus.COMPLETED, date
        );

        return DailyCompletedMissionsResponseDto.from(date, completed);
    }
}
