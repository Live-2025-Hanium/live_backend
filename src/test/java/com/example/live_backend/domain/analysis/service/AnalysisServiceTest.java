package com.example.live_backend.domain.analysis.service;

import com.example.live_backend.domain.analysis.controller.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.analysis.controller.dto.WeeklyMissionSummaryResponseDto;
import com.example.live_backend.domain.analysis.controller.dto.DailyCompletedMissionsResponseDto;
import com.example.live_backend.domain.analysis.controller.service.AnalysisService;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisService 테스트")
class AnalysisServiceTest {

    @InjectMocks
    private AnalysisService analysisService;

    @Mock
    private CloverMissionRecordRepository cloverMissionRecordRepository;

    @Nested
    @DisplayName("getMonthlyParticipation()")
    class GetMonthlyParticipation {

        @Test
        @DisplayName("성공 - 참여율 정상 계산 및 Repository 호출 파라미터 검증")
        void computesRateAndCallsRepositoryWithCorrectDates() {
            // Given
            Long memberId = 42L;
            YearMonth ym = YearMonth.of(2025, 8);
            long assigned = 10L;
            long completed = 7L;

            given(cloverMissionRecordRepository.countAssignedInPeriod(eq(memberId), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(assigned);
            given(cloverMissionRecordRepository.countCompletedInPeriod(eq(memberId), eq(CloverMissionStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(completed);

            // When
            MonthlyParticipationResponseDto result = analysisService.getMonthlyParticipation(memberId, ym);

            // Then: 계산값 검증
            assertThat(result.getYear()).isEqualTo(ym.getYear());
            assertThat(result.getMonth()).isEqualTo(ym.getMonthValue());
            assertThat(result.getTotalAssigned()).isEqualTo(assigned);
            assertThat(result.getTotalCompleted()).isEqualTo(completed);
            assertThat(result.getCompletionRate()).isEqualTo(70.0);

            // Then: Repository 호출 파라미터 검증
            ArgumentCaptor<LocalDate> startDateCap = ArgumentCaptor.forClass(LocalDate.class);
            ArgumentCaptor<LocalDate> endDateCap = ArgumentCaptor.forClass(LocalDate.class);
            verify(cloverMissionRecordRepository)
                    .countAssignedInPeriod(eq(memberId), startDateCap.capture(), endDateCap.capture());
            assertThat(startDateCap.getValue()).isEqualTo(ym.atDay(1));
            assertThat(endDateCap.getValue()).isEqualTo(ym.atEndOfMonth());

            ArgumentCaptor<LocalDateTime> startDateTimeCap = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<LocalDateTime> endDateTimeCap = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(cloverMissionRecordRepository)
                    .countCompletedInPeriod(eq(memberId), eq(CloverMissionStatus.COMPLETED), startDateTimeCap.capture(), endDateTimeCap.capture());
            assertThat(startDateTimeCap.getValue()).isEqualTo(ym.atDay(1).atStartOfDay());
            assertThat(endDateTimeCap.getValue()).isEqualTo(ym.atEndOfMonth().atTime(LocalTime.MAX));
        }

        @Test
        @DisplayName("성공 - 할당 미션이 0건인 경우 완료율은 0.0")
        void assignedZero_YieldsZeroRate() {

            // Given
            Long memberId = 7L;
            YearMonth ym = YearMonth.of(2025, 8);

            given(cloverMissionRecordRepository.countAssignedInPeriod(eq(memberId), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(0L);
            // 할당 미션이 0건이면 당연히 completed 도 0이어햐함
            given(cloverMissionRecordRepository.countCompletedInPeriod(eq(memberId), eq(CloverMissionStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0L);

            // When
            MonthlyParticipationResponseDto result = analysisService.getMonthlyParticipation(memberId, ym);

            // Then
            assertThat(result.getTotalAssigned()).isEqualTo(0L);
            assertThat(result.getTotalCompleted()).isEqualTo(0L);
            assertThat(result.getCompletionRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("성공 - 완료 미션이 0건인 경우 완료율은 0.0")
        void completedZero_YieldsZeroRate() {

            // Given
            Long memberId = 7L;
            YearMonth ym = YearMonth.of(2025, 8);

            given(cloverMissionRecordRepository.countAssignedInPeriod(eq(memberId), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(10L);

            given(cloverMissionRecordRepository.countCompletedInPeriod(eq(memberId), eq(CloverMissionStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0L);

            // When
            MonthlyParticipationResponseDto result = analysisService.getMonthlyParticipation(memberId, ym);

            // Then
            assertThat(result.getTotalAssigned()).isEqualTo(10L);
            assertThat(result.getTotalCompleted()).isEqualTo(0L);
            assertThat(result.getCompletionRate()).isEqualTo(0.0);

        }
    }

    @Nested
    @DisplayName("getWeeklySummary()")
    class GetWeeklySummary {

        @Test
        @DisplayName("성공 - 주간 완료 목록 조회 및 주차 범위/호출 파라미터 검증")
        void returnsWeeklySummary_AndCallsRepositoryWithWeekRange() {

            // Given
            Long memberId = 55L;
            LocalDate date = LocalDate.of(2025, 8, 14);
            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = date.with(DayOfWeek.SUNDAY);
            LocalDateTime startDateTime = weekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

            CloverMissionRecord r1 = CloverMissionRecord.builder()
                    .missionId(1L)
                    .missionTitle("M1")
                    .completedAt(weekStart.atTime(10, 0))
                    .build();

            CloverMissionRecord r2 = CloverMissionRecord.builder()
                    .missionId(2L)
                    .missionTitle("M2")
                    .completedAt(weekStart.plusDays(2).atTime(12, 0))
                    .build();

            CloverMissionRecord r3 = CloverMissionRecord.builder()
                    .missionId(3L)
                    .missionTitle("M3")
                    .completedAt(weekEnd.atTime(18, 0))
                    .build();

            List<CloverMissionRecord> completed = List.of(r1, r2, r3);

            given(cloverMissionRecordRepository.findCompletedInPeriod(
                    eq(memberId),
                    eq(CloverMissionStatus.COMPLETED),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).willReturn(completed);

            // When
            WeeklyMissionSummaryResponseDto result = analysisService.getWeeklySummary(memberId, date);

            // Then
            assertThat(result.getWeekStartDate()).isEqualTo(weekStart);
            assertThat(result.getWeekEndDate()).isEqualTo(weekEnd);
            assertThat(result.getWeeklySummary().size()).isEqualTo(7);
            assertThat(result.getWeeklySummary().get(0).getDate()).isEqualTo(weekStart);
            assertThat(result.getWeeklySummary().get(0).getCloverMissionCount()).isEqualTo(1);
            assertThat(result.getWeeklySummary().get(2).getDate()).isEqualTo(weekStart.plusDays(2));
            assertThat(result.getWeeklySummary().get(2).getCloverMissionCount()).isEqualTo(1);
            assertThat(result.getWeeklySummary().get(6).getDate()).isEqualTo(weekEnd);
            assertThat(result.getWeeklySummary().get(6).getCloverMissionCount()).isEqualTo(1);

            ArgumentCaptor<LocalDateTime> startCap = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<LocalDateTime> endCap = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(cloverMissionRecordRepository).findCompletedInPeriod(
                    eq(memberId),
                    eq(CloverMissionStatus.COMPLETED),
                    startCap.capture(),
                    endCap.capture()
            );
            assertThat(startCap.getValue()).isEqualTo(startDateTime);
            assertThat(endCap.getValue()).isEqualTo(endDateTime);
        }
    }

    @Nested
    @DisplayName("getDailyCompleted()")
    class GetDailyCompleted {

        @Test
        @DisplayName("성공 - 일간 완료 목록 조회 및 호출 파라미터 검증")
        void returnsDailyCompleted_AndCallsRepositoryWithDate() {

            // Given
            Long memberId = 77L;
            LocalDate date = LocalDate.of(2025, 8, 16);

            CloverMissionRecord r1 = CloverMissionRecord.builder()
                    .missionId(101L)
                    .missionTitle("Daily-1")
                    .completedAt(date.atTime(9, 0))
                    .build();

            CloverMissionRecord r2 = CloverMissionRecord.builder()
                    .missionId(102L)
                    .missionTitle("Daily-2")
                    .completedAt(date.atTime(15, 30))
                    .build();

            List<CloverMissionRecord> completed = List.of(r1, r2);

            given(cloverMissionRecordRepository.findCompletedOnDate(
                    eq(memberId),
                    eq(CloverMissionStatus.COMPLETED),
                    eq(date)
            )).willReturn(completed);

            // When
            DailyCompletedMissionsResponseDto result = analysisService.getDailyCompleted(memberId, date);

            // Then
            assertThat(result.getDate()).isEqualTo(date);
            assertThat(result.getDayOfWeek()).isEqualTo(date.getDayOfWeek());
            assertThat(result.getCompletedMissions().size()).isEqualTo(2);
            assertThat(result.getCompletedMissions().get(0).getUserMissionId()).isEqualTo(101L);
            assertThat(result.getCompletedMissions().get(0).getMissionTitle()).isEqualTo("Daily-1");
            assertThat(result.getCompletedMissions().get(1).getUserMissionId()).isEqualTo(102L);
            assertThat(result.getCompletedMissions().get(1).getMissionTitle()).isEqualTo("Daily-2");

            verify(cloverMissionRecordRepository).findCompletedOnDate(
                    eq(memberId),
                    eq(CloverMissionStatus.COMPLETED),
                    eq(date)
            );
        }
    }
}