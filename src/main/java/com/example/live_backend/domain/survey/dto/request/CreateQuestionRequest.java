package com.example.live_backend.domain.survey.dto.request;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateQuestionRequest {
    
    @NotNull(message = "질문 번호는 필수입니다")
    private Integer questionNumber;
    
    @NotBlank(message = "질문 내용은 필수입니다")
    private String questionText;
    
    @NotNull(message = "질문 유형은 필수입니다")
    private SurveyQuestion.QuestionType questionType;
    
    private boolean isRequired = true;
    
    private boolean isActive = true;
    
    private List<CreateOptionRequest> options;
    
    @Getter
    @NoArgsConstructor
    public static class CreateOptionRequest {
        @NotNull(message = "옵션 번호는 필수입니다")
        private Integer optionNumber;
        
        @NotBlank(message = "옵션 내용은 필수입니다")
        private String optionText;
        
        private boolean isActive = true;
    }
}