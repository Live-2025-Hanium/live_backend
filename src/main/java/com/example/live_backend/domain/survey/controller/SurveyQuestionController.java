package com.example.live_backend.domain.survey.controller;

import com.example.live_backend.domain.survey.controller.docs.SurveyQuestionControllerDocs;
import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.dto.request.UpdateQuestionRequest;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.service.SurveyQuestionService;
import com.example.live_backend.global.error.response.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.live_backend.global.security.annotation.AdminApi;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/survey/questions")
@RequiredArgsConstructor
public class SurveyQuestionController implements SurveyQuestionControllerDocs {
    
    private final SurveyQuestionService surveyQuestionService;
    
    @Override
    @GetMapping
    public ResponseHandler<List<SurveyQuestionDto>> getAllActiveQuestions() {
        List<SurveyQuestionDto> questions = surveyQuestionService.getAllActiveQuestions();
        return ResponseHandler.success(questions);
    }
    
    @Override
    @PostMapping
    @AdminApi(reason = "설문 질문 생성은 관리자만 가능합니다")
    public ResponseHandler<SurveyQuestionDto> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {
        SurveyQuestionDto created = surveyQuestionService.createQuestion(request);
        return ResponseHandler.success(created);
    }
    
    @Override
    @PutMapping("/{questionId}")
    @AdminApi(reason = "설문 질문 수정은 관리자만 가능합니다")
    public ResponseHandler<SurveyQuestionDto> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {
        SurveyQuestionDto updated = surveyQuestionService.updateQuestion(questionId, request);
        return ResponseHandler.success(updated);
    }
    
    @Override
    @DeleteMapping("/{questionId}")
    @AdminApi(reason = "설문 질문 비활성화는 관리자만 가능합니다")
    public ResponseHandler<Void> deactivateQuestion(
            @PathVariable Long questionId) {
        surveyQuestionService.deactivateQuestion(questionId);
        return ResponseHandler.success(null);
    }
}