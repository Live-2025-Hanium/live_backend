package com.example.live_backend.domain.survey.dto.request;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateQuestionRequest {
    
    @NotBlank(message = "질문 내용은 필수입니다")
    private String questionText;
    
    @NotNull(message = "질문 유형은 필수입니다")
    private SurveyQuestion.QuestionType questionType;
    
    private boolean isRequired;
    
    private boolean isActive;
}