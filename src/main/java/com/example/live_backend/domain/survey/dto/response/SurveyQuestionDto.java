package com.example.live_backend.domain.survey.dto.response;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class SurveyQuestionDto {
    
    private Long id;
    private Integer questionNumber;
    private String questionText;
    private boolean isRequired;
    private List<SurveyQuestionOptionDto> options;
    
    public static SurveyQuestionDto from(SurveyQuestion question) {
        return SurveyQuestionDto.builder()
                .id(question.getId())
                .questionNumber(question.getQuestionNumber())
                .questionText(question.getQuestionText())
                .isRequired(question.isRequired())
                .options(question.getOptions().stream()
                        .filter(option -> option.isActive())
                        .map(SurveyQuestionOptionDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}