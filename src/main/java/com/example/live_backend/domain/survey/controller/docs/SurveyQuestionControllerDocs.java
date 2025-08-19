package com.example.live_backend.domain.survey.controller.docs;

import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.dto.request.UpdateQuestionRequest;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Survey Question", description = "설문 질문 관리 API")
public interface SurveyQuestionControllerDocs {

    @Operation(summary = "활성 설문 질문 목록 조회", 
        description = "현재 활성화된 모든 설문 질문과 옵션을 조회합니다. 프론트엔드에서 설문 화면을 구성할 때 사용합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SurveyQuestionDto.class)))
    })
    ResponseEntity<List<SurveyQuestionDto>> getAllActiveQuestions();

    @Operation(summary = "새 질문 생성", 
        description = "새로운 설문 질문을 생성합니다. 객관식 질문의 경우 선택지도 함께 생성됩니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = SurveyQuestionDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 중복된 질문 번호",
            content = @Content(examples = @ExampleObject(value = "{\"message\": \"이미 존재하는 질문 번호입니다: 1\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<SurveyQuestionDto> createQuestion(
        @Valid @RequestBody CreateQuestionRequest request
    );

    @Operation(summary = "질문 수정", 
        description = "기존 설문 질문을 수정합니다. 질문 텍스트, 필수 여부, 활성화 상태를 변경할 수 있습니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = SurveyQuestionDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    })
    ResponseEntity<SurveyQuestionDto> updateQuestion(
        @Parameter(description = "질문 ID", required = true) @PathVariable Long questionId,
        @Valid @RequestBody UpdateQuestionRequest request
    );

    @Operation(summary = "질문 비활성화", 
        description = "설문 질문을 비활성화합니다. 비활성화된 질문은 사용자에게 표시되지 않습니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "비활성화 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    })
    ResponseEntity<Void> deactivateQuestion(
        @Parameter(description = "질문 ID", required = true) @PathVariable Long questionId
    );
}