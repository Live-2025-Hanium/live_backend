package com.example.live_backend.domain.analysis.controller.docs;

import com.example.live_backend.domain.analysis.dto.*;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Analysis", description = "미션 분석(통계) API")
public interface AnalysisControllerDocs {

    @Operation(summary = "월별 클로버 미션 완료율 조회", description = "월별 클로버 미션 완료율을 조회합니다.")
    ResponseHandler<MonthlyParticipationResponseDto> getParticipation(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @Parameter(description = "조회 기준 년,월", example = "2025-08")
            @RequestParam(name = "yearMonth") String yearMonth
    );

    @Operation(summary = "주간 클로버 미션 완료 현황 조회", description = "지정한 날짜가 포함된 주간의 클로버 미션 완료 현황을 조회합니다. date 필수(2025-08-15)")
    ResponseHandler<WeeklyMissionSummaryResponseDto> getWeeklyMissions(
            @Parameter(description = "조회 기준 날짜", example = "2025-08-15")
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "일간 클로버 미션 완료 목록 조회", description = "지정한 날짜의 완료된 클로버 미션 목록을 조회합니다. date 필수(2025-08-15)")
    ResponseHandler<DailyCompletedMissionsResponseDto> getDailyMissions(
            @Parameter(description = "조회 기준 날짜", example = "2025-08-15")
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "전월 대비 클로버 미션 TOP3 성장 카테고리 조회", description = "현재 월 기준 전월 대비 성장한 TOP3 클로버 미션의 카테고리를 조회합니다.")
    ResponseHandler<MonthlyGrowthResponseDto> getMonthlyGrowth(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이미션 월별 완료율 조회", description = "마이미션의 월별 완료율을 조회합니다.")
    ResponseHandler<MonthlyMyMissionParticipationResponseDto> getMyMissionParticipation(
            @Parameter(description = "조회 기준 년,월", example = "2025-10")
            @RequestParam(name = "yearMonth") String yearMonth,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이미션 주간 완료 현황 조회", description = "지정한 날짜가 포함된 주간의 마이미션 완료 현황을 조회합니다. date 필수(2025-10-01)")
    ResponseHandler<WeeklyMyMissionSummaryResponseDto> getWeeklyMyMissions(
            @Parameter(description = "조회 기준 날짜", example = "2025-10-01")
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이미션 일별 완료 목록 조회", description = "지정한 날짜에 완료된 마이미션 목록을 조회합니다. date 필수(2025-10-01)")
    ResponseHandler<DailyCompletedMyMissionsResponseDto> getDailyMyMissions(
            @Parameter(description = "조회 기준 날짜", example = "2025-10-01")
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );
}