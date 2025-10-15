package com.example.live_backend.domain.analysis.controller;

import com.example.live_backend.domain.analysis.dto.*;
import com.example.live_backend.domain.analysis.service.AnalysisService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.YearMonth;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisController 테스트")
class AnalysisControllerTest {

    @InjectMocks
    private AnalysisController analysisController;

    @Mock
    private AnalysisService analysisService;

    private PrincipalDetails member;
    private static final long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        member = new PrincipalDetails(
                MEMBER_ID,
                "oauth-" + MEMBER_ID,
                "USER",
                "닉",
                "user" + MEMBER_ID + "@example.com"
        );
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/clover/participation")
    class GetParticipation {

        @Test
        @DisplayName("성공 및 서비스 위임 확인 - 월별 클로버 미션 완료율 조회")
        void returnsSuccessAndDelegatesToService() {
            // Given
            YearMonth expectedYm = YearMonth.now();
            MonthlyParticipationResponseDto dto = MonthlyParticipationResponseDto.from(10L, 7L, 70.0);
            given(analysisService.getMonthlyParticipation(eq(MEMBER_ID), any(YearMonth.class))).willReturn(dto);

            // When
            ResponseHandler<MonthlyParticipationResponseDto> response = analysisController.getParticipation(member,String.valueOf(expectedYm));

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);

            ArgumentCaptor<YearMonth> ymCaptor = ArgumentCaptor.forClass(YearMonth.class);
            verify(analysisService).getMonthlyParticipation(eq(MEMBER_ID), ymCaptor.capture());
            assertThat(ymCaptor.getValue()).isEqualTo(expectedYm);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/clover (weekly/daily 개별 엔드포인트)")
    class GetMissions {

        @Test
        @DisplayName("WEEKLY 조회 성공 및 서비스 위임 확인")
        void weekly_ReturnsSuccessAndDelegatesToService() {
            // Given
            LocalDate date = LocalDate.of(2025, 8, 15);
            WeeklyMissionSummaryResponseDto dto = WeeklyMissionSummaryResponseDto.from(
                    date.with(DayOfWeek.MONDAY),
                    date.with(DayOfWeek.SUNDAY),
                    Collections.emptyList()
            );
            given(analysisService.getWeeklySummary(eq(MEMBER_ID), eq(date))).willReturn(dto);

            // When
            ResponseHandler<WeeklyMissionSummaryResponseDto> response = analysisController.getWeeklyMissions(date, member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);
            verify(analysisService).getWeeklySummary(eq(MEMBER_ID), eq(date));
        }

        @Test
        @DisplayName("DAILY 조회 성공 및 서비스 위임 확인")
        void daily_ReturnsSuccessAndDelegatesToService() {
            // Given
            LocalDate date = LocalDate.of(2025, 8, 16);
            DailyCompletedMissionsResponseDto dto = DailyCompletedMissionsResponseDto.from(date, Collections.emptyList());
            given(analysisService.getDailyCompleted(eq(MEMBER_ID), eq(date))).willReturn(dto);

            // When
            ResponseHandler<DailyCompletedMissionsResponseDto> response = analysisController.getDailyMissions(date, member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);
            verify(analysisService).getDailyCompleted(eq(MEMBER_ID), eq(date));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/clover/monthly-growth")
    class GetMonthlyGrowth {

        @Test
        @DisplayName("성공 및 서비스 위임 확인 - 전월 대비 TOP3 성장 카테고리")
        void returnsSuccessAndDelegatesToService() {
            // Given
            YearMonth expectedYm = YearMonth.now();

            MonthlyGrowthResponseDto.GrowthSummary g1 =
                    MonthlyGrowthResponseDto.GrowthSummary.builder()
                            .rank(1)
                            .categoryName("환경 바꾸기")
                            .previousMonthCount(5)
                            .currentMonthCount(7)
                            .growthPercentage(40.0)
                            .build();

            MonthlyGrowthResponseDto dto =
                    MonthlyGrowthResponseDto.builder()
                            .previousMonth(expectedYm.minusMonths(1).getMonthValue())
                            .currentMonth(expectedYm.getMonthValue())
                            .growthSummary(java.util.List.of(g1))
                            .build();

            given(analysisService.getMonthlyGrowthTop3(eq(MEMBER_ID), any(YearMonth.class))).willReturn(dto);

            // When
            ResponseHandler<MonthlyGrowthResponseDto> response = analysisController.getMonthlyGrowth(member, String.valueOf(expectedYm));

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);

            ArgumentCaptor<YearMonth> ymCaptor = ArgumentCaptor.forClass(YearMonth.class);
            verify(analysisService).getMonthlyGrowthTop3(eq(MEMBER_ID), ymCaptor.capture());
            assertThat(ymCaptor.getValue()).isEqualTo(expectedYm);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/my/participation")
    class GetMyMissionCompletionRate {

        @Test
        @DisplayName("성공 및 서비스 위임 확인 - 마이미션 월별 완료율 조회")
        void returnsSuccessAndDelegatesToService() {
            // Given
            YearMonth expectedYm = YearMonth.of(2025, 10);
            MonthlyMyMissionParticipationResponseDto dto =
                    MonthlyMyMissionParticipationResponseDto.from(15L, 12L, 80.0);
            given(analysisService.getMonthlyMyMissionCompletionRate(eq(MEMBER_ID), any(YearMonth.class)))
                    .willReturn(dto);

            // When
            ResponseHandler<MonthlyMyMissionParticipationResponseDto> response =
                    analysisController.getMyMissionParticipation("2025-10", member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);
            assertThat(response.getData().getTotalAssigned()).isEqualTo(15L);
            assertThat(response.getData().getTotalCompleted()).isEqualTo(12L);
            assertThat(response.getData().getCompletionRate()).isEqualTo(80.0);

            ArgumentCaptor<YearMonth> ymCaptor = ArgumentCaptor.forClass(YearMonth.class);
            verify(analysisService).getMonthlyMyMissionCompletionRate(eq(MEMBER_ID), ymCaptor.capture());
            assertThat(ymCaptor.getValue()).isEqualTo(expectedYm);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/my/weekly")
    class GetWeeklyMyMissions {

        @Test
        @DisplayName("성공 및 서비스 위임 확인 - 마이미션 주간 완료 현황 조회")
        void returnsSuccessAndDelegatesToService() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 1);
            WeeklyMyMissionSummaryResponseDto dto = WeeklyMyMissionSummaryResponseDto.from(
                    date.with(DayOfWeek.MONDAY),
                    date.with(DayOfWeek.SUNDAY),
                    Collections.emptyList()
            );
            given(analysisService.getWeeklyMyMissionSummary(eq(MEMBER_ID), eq(date))).willReturn(dto);

            // When
            ResponseHandler<WeeklyMyMissionSummaryResponseDto> response =
                    analysisController.getWeeklyMyMissions(date, member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);
            assertThat(response.getData().getWeekStartDate()).isEqualTo(date.with(DayOfWeek.MONDAY));
            assertThat(response.getData().getWeekEndDate()).isEqualTo(date.with(DayOfWeek.SUNDAY));
            verify(analysisService).getWeeklyMyMissionSummary(eq(MEMBER_ID), eq(date));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/my/daily")
    class GetDailyMyMissions {

        @Test
        @DisplayName("성공 및 서비스 위임 확인 - 마이미션 일별 완료 목록 조회")
        void returnsSuccessAndDelegatesToService() {
            // Given
            LocalDate date = LocalDate.of(2025, 10, 1);
            DailyCompletedMyMissionsResponseDto dto =
                    DailyCompletedMyMissionsResponseDto.from(date, Collections.emptyList());
            given(analysisService.getDailyCompletedMyMissions(eq(MEMBER_ID), eq(date))).willReturn(dto);

            // When
            ResponseHandler<DailyCompletedMyMissionsResponseDto> response =
                    analysisController.getDailyMyMissions(date, member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);
            assertThat(response.getData().getDate()).isEqualTo(date);
            assertThat(response.getData().getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
            verify(analysisService).getDailyCompletedMyMissions(eq(MEMBER_ID), eq(date));
        }
    }
}