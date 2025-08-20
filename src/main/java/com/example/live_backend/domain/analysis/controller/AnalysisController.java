package com.example.live_backend.domain.analysis.controller;

import com.example.live_backend.domain.analysis.controller.docs.AnalysisControllerDocs;
import com.example.live_backend.domain.analysis.dto.DailyCompletedMissionsResponseDto;
import com.example.live_backend.domain.analysis.dto.MonthlyGrowthResponseDto;
import com.example.live_backend.domain.analysis.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.analysis.dto.WeeklyMissionSummaryResponseDto;
import com.example.live_backend.domain.analysis.service.AnalysisService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis")
public class AnalysisController implements AnalysisControllerDocs {

    private final AnalysisService analysisService;

    @Override
    @AuthenticatedApi(reason = "금월 미션 완료율 조회는 로그인한 사용자만 가능합니다")
    @GetMapping("/participation")
    public ResponseHandler<MonthlyParticipationResponseDto> getParticipation(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        YearMonth ym = YearMonth.now();
        return ResponseHandler.success(analysisService.getMonthlyParticipation(memberId, ym));
    }

    @Override
    @AuthenticatedApi(reason = "주간 미션 완료 현황 조회는 로그인한 사용자만 가능합니다")
    @GetMapping("/missions/weekly")
    public ResponseHandler<WeeklyMissionSummaryResponseDto> getWeeklyMissions(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        return ResponseHandler.success(analysisService.getWeeklySummary(memberId, date));
    }

    @Override
    @AuthenticatedApi(reason = "일간 미션 완료 현황 조회는 로그인한 사용자만 가능합니다")
    @GetMapping("/missions/daily")
    public ResponseHandler<DailyCompletedMissionsResponseDto> getDailyMissions(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        return ResponseHandler.success(analysisService.getDailyCompleted(memberId, date));
    }

    @Override
    @AuthenticatedApi(reason = "전월 대비 클로버 미션 TOP3 성장 카테고리 조회는 로그인한 사용자만 가능합니다")
    @GetMapping("/monthly-growth")
    public ResponseHandler<MonthlyGrowthResponseDto> getMonthlyGrowth(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        YearMonth ym = YearMonth.now();

        return ResponseHandler.success(analysisService.getMonthlyGrowthTop3(memberId, ym));
    }
}