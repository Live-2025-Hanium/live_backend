package com.example.live_backend.domain.survey.controller;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.domain.survey.controller.docs.SurveyControllerDocs;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import com.example.live_backend.domain.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import com.example.live_backend.global.security.annotation.AdminApi;
import com.example.live_backend.global.security.PrincipalDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/surveys")
@Slf4j
public class SurveyController implements SurveyControllerDocs {

	private final SurveyService surveyService;

	@Override
	@PostMapping("/submit")
	@AuthenticatedApi(reason = "설문 제출은 로그인한 사용자만 가능합니다")
	public ResponseHandler<SurveySubmissionResponseDto> submitSurvey(
		@Valid @RequestBody SurveySubmissionDto request,
		@AuthenticationPrincipal PrincipalDetails userDetails) {

		log.info("설문 응답 제출 요청 - 답변 수: {}", request.getAnswers().size());

		SurveySubmissionResponseDto response = surveyService.submitSurvey(request, userDetails.getMemberId());

		return ResponseHandler.success(response);
	}


	@Override
	@GetMapping("/admin/users/{userId}/responses")
	@AdminApi(reason = "사용자별 설문 응답 조회는 관리자만 가능합니다")
	public ResponseHandler<SurveyResponseListDto.UserSurveyResponseListDto> getUserSurveyResponses(
		@PathVariable Long userId) {

		log.info("사용자별 설문 응답 조회 요청 - 사용자 ID: {}", userId);

		SurveyResponseListDto.UserSurveyResponseListDto response = surveyService.getUserSurveyResponses(userId);

		return ResponseHandler.success(response);
	}

	@Override
	@GetMapping("/admin/responses")
	@AdminApi(reason = "기간별 설문 응답 조회는 관리자만 가능합니다")
	public ResponseHandler<List<SurveyResponseListDto>> getSurveyResponsesByPeriod(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.info("기간별 설문 응답 조회 요청 - 시작: {}, 종료: {}", startDate, endDate);

		List<SurveyResponseListDto> responses = surveyService.getSurveyResponsesByPeriod(startDate, endDate);

		return ResponseHandler.success(responses);
	}
}