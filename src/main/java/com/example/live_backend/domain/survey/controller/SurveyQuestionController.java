package com.example.live_backend.domain.survey.controller;

import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.dto.request.UpdateQuestionRequest;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.service.SurveyQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/survey/questions")
@RequiredArgsConstructor
@Tag(name = "Survey Question", description = "설문 질문 관리 API")
public class SurveyQuestionController {
    
    private final SurveyQuestionService surveyQuestionService;
    
    @GetMapping
    @Operation(summary = "활성 설문 질문 목록 조회", description = "현재 활성화된 모든 설문 질문과 옵션을 조회합니다")
    public ResponseEntity<List<SurveyQuestionDto>> getAllActiveQuestions() {
        List<SurveyQuestionDto> questions = surveyQuestionService.getAllActiveQuestions();
        return ResponseEntity.ok(questions);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "새 질문 생성", description = "새로운 설문 질문을 생성합니다 (관리자 전용)")
    public ResponseEntity<SurveyQuestionDto> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {
        SurveyQuestionDto created = surveyQuestionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "질문 수정", description = "기존 설문 질문을 수정합니다 (관리자 전용)")
    public ResponseEntity<SurveyQuestionDto> updateQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {
        SurveyQuestionDto updated = surveyQuestionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "질문 비활성화", description = "설문 질문을 비활성화합니다 (관리자 전용)")
    public ResponseEntity<Void> deactivateQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId) {
        surveyQuestionService.deactivateQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}