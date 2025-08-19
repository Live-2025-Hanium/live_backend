package com.example.live_backend.domain.survey.controller.docs;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Survey", description = "설문 응답 관련 API")
public interface SurveyControllerDocs {

    @Operation(summary = "설문 응답 제출", 
        description = "설문 응답을 제출합니다. 15문제에 대한 설문을 모두 작성한 뒤 응답 제출을 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "제출 성공",
            content = @Content(schema = @Schema(implementation = SurveySubmissionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값 (답변 개수 부족, 중복 문제 등)",
            content = @Content(examples = @ExampleObject(value = "{\"message\": \"설문 문제는 총 15개입니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseHandler<SurveySubmissionResponseDto> submitSurvey(
        @Valid @RequestBody SurveySubmissionDto request
    );

    @Operation(summary = "사용자별 설문 응답 조회", 
        description = "특정 사용자의 설문 응답 목록과 총 응답 횟수를 조회합니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SurveyResponseListDto.UserSurveyResponseListDto.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseHandler<SurveyResponseListDto.UserSurveyResponseListDto> getUserSurveyResponses(
        @Parameter(description = "사용자 ID", example = "1", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "기간별 설문 응답 조회", 
        description = "특정 기간의 모든 설문 응답을 조회합니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SurveyResponseListDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseHandler<List<SurveyResponseListDto>> getSurveyResponsesByPeriod(
        @Parameter(description = "시작 날짜", example = "2024-01-01", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "종료 날짜", example = "2024-01-31", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}