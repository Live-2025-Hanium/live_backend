package com.example.live_backend.domain.survey.controller;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import com.example.live_backend.domain.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/surveys")
@Tag(name = "Survey", description = "설문 관련 API")
@Slf4j
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping("/submit")
    @Operation(summary = "설문 응답 제출 ",
               description = "설문 응답을 제출 합니다. 5문제에 대한 설문을 모두 작성한 뒤 응답 제출을 합니다. ")
    public ResponseHandler<SurveySubmissionResponseDto> submitSurvey(
            @Valid @RequestBody SurveySubmissionDto request) {
        
        log.info("설문 응답 제출 요청 - 답변 수: {}", request.getAnswers().size());
        
        SurveySubmissionResponseDto response = surveyService.submitSurvey(request);
        
        return ResponseHandler.response(response);
    }


 /*
 아래는 admin용 api 입니다.
 */
    @GetMapping("/admin/users/{userId}/responses")
    @Operation(summary = "사용자별 설문 응답 조회", description = "특정 사용자의 설문 응답 목록을 조회합니다. (관리자용)")
    public ResponseHandler<List<SurveyResponseListDto>> getUserSurveyResponses(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId) {
        
        log.info("사용자별 설문 응답 조회 요청 - 사용자 ID: {}", userId);
        
        List<SurveyResponseListDto> responses = surveyService.getUserSurveyResponses(userId);
        
        return ResponseHandler.response(responses);
    }

    @GetMapping("/admin/responses")
    @Operation(summary = "기간별 설문 응답 조회", description = "특정 기간의 모든 설문 응답을 조회합니다. (관리자용)")
    public ResponseHandler<List<SurveyResponseListDto>> getSurveyResponsesByPeriod(
            @Parameter(description = "시작 일시", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 일시", example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("기간별 설문 응답 조회 요청 - 시작: {}, 종료: {}", startDate, endDate);
        
        List<SurveyResponseListDto> responses = surveyService.getSurveyResponsesByPeriod(startDate, endDate);
        
        return ResponseHandler.response(responses);
    }

    @GetMapping("/admin/users/{userId}/count")
    @Operation(summary = "사용자 설문 응답 횟수 조회", description = "특정 사용자의 설문 응답 횟수를 조회합니다. (관리자용)")
    public ResponseHandler<Long> getUserSurveyCount(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId) {
        
        log.info("사용자 설문 응답 횟수 조회 요청 - 사용자 ID: {}", userId);
        
        Long count = surveyService.getUserSurveyCount(userId);
        
        return ResponseHandler.response(count);
    }
} 