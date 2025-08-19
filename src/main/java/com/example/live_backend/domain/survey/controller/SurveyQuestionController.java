package com.example.live_backend.domain.survey.controller;

import com.example.live_backend.domain.survey.controller.docs.SurveyQuestionControllerDocs;
import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.dto.request.UpdateQuestionRequest;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.service.SurveyQuestionService;
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
public class SurveyQuestionController implements SurveyQuestionControllerDocs {
    
    private final SurveyQuestionService surveyQuestionService;
    
    @Override
    @GetMapping
    public ResponseEntity<List<SurveyQuestionDto>> getAllActiveQuestions() {
        List<SurveyQuestionDto> questions = surveyQuestionService.getAllActiveQuestions();
        return ResponseEntity.ok(questions);
    }
    
    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyQuestionDto> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {
        SurveyQuestionDto created = surveyQuestionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Override
    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyQuestionDto> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {
        SurveyQuestionDto updated = surveyQuestionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(updated);
    }
    
    @Override
    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateQuestion(
            @PathVariable Long questionId) {
        surveyQuestionService.deactivateQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}