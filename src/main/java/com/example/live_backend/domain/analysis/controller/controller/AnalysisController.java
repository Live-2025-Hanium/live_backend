package com.example.live_backend.domain.analysis.controller.controller;

import com.example.live_backend.domain.analysis.controller.dto.MonthlyParticipationResponseDto;
import com.example.live_backend.domain.analysis.controller.service.AnalysisService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis")
@Tag(name = "Analytics", description = "미션 통계 API")
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/participation")
    @Operation(summary = "월별 미션 완료율", description = "해당 월의 미션 완료율을 조회합니다. date 미지정 시 현재 월 기준.")
    public ResponseHandler<MonthlyParticipationResponseDto> getParticipation(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        YearMonth ym = YearMonth.now();
        return ResponseHandler.success(analysisService.getMonthlyParticipation(userDetails.getMemberId(), ym));
    }
}
