package com.example.live_backend.domain.analysis.controller;

import com.example.live_backend.domain.analysis.dto.DailyCompletedMissionsResponseDto;
import com.example.live_backend.domain.analysis.dto.MonthlyGrowthResponseDto;
import com.example.live_backend.domain.analysis.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.analysis.dto.WeeklyMissionSummaryResponseDto;
import com.example.live_backend.domain.analysis.service.AnalysisService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @DisplayName("GET /api/v1/analysis/participation")
    class GetParticipation {

        @Test
        @DisplayName("성공 및 서비스 위임 확인 - 월별 미션 완료율 조회")
        void returnsSuccessAndDelegatesToService() {
            // Given
            YearMonth expectedYm = YearMonth.now();
            MonthlyParticipationResponseDto dto = MonthlyParticipationResponseDto.from(expectedYm, 10L, 7L, 70.0);
            given(analysisService.getMonthlyParticipation(eq(MEMBER_ID), any(YearMonth.class))).willReturn(dto);

            // When
            ResponseHandler<MonthlyParticipationResponseDto> response = analysisController.getParticipation(member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);

            ArgumentCaptor<YearMonth> ymCaptor = ArgumentCaptor.forClass(YearMonth.class);
            verify(analysisService).getMonthlyParticipation(eq(MEMBER_ID), ymCaptor.capture());
            assertThat(ymCaptor.getValue()).isEqualTo(expectedYm);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/missions")
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
            ResponseHandler<?> response = analysisController.getMissions(date, "weekly", member);

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
            ResponseHandler<?> response = analysisController.getMissions(date, "daily", member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);
            verify(analysisService).getDailyCompleted(eq(MEMBER_ID), eq(date));
        }

        @Test
        @DisplayName("실패 - 잘못된 type 전달 시 예외 발생")
        void invalidType_ThrowsCustomException() {
            // Given
            LocalDate date = LocalDate.of(2025, 8, 17);

            // When & Then
            CustomException ex = assertThrows(CustomException.class,
                    () -> analysisController.getMissions(date, "monthly", member));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_QUERY_TYPE);
        }

        @Test
        @DisplayName("실패 - type 이 없으면 예외 발생")
        void missingType_ThrowsCustomException() {
            // Given
            LocalDate date = LocalDate.of(2025, 8, 18);

            // When & Then
            CustomException ex = assertThrows(CustomException.class,
                    () -> analysisController.getMissions(date, null, member));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUERY_TYPE_REQUIRED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/monthly-growth")
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
            ResponseHandler<MonthlyGrowthResponseDto> response = analysisController.getMonthlyGrowth(member);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(dto);

            ArgumentCaptor<YearMonth> ymCaptor = ArgumentCaptor.forClass(YearMonth.class);
            verify(analysisService).getMonthlyGrowthTop3(eq(MEMBER_ID), ymCaptor.capture());
            assertThat(ymCaptor.getValue()).isEqualTo(expectedYm);
        }
    }
}