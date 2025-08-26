package com.example.live_backend.domain.survey.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateQuestionRequest {
    
    @NotBlank(message = "질문 내용은 필수입니다")
    private String questionText;
    
    private boolean isRequired;
    
    private boolean isActive;
}