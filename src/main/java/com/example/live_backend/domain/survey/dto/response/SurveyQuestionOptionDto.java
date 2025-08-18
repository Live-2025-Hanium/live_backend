package com.example.live_backend.domain.survey.dto.response;

import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyQuestionOptionDto {
    
    private Long id;
    private Integer optionNumber;
    private String optionText;
    
    public static SurveyQuestionOptionDto from(SurveyQuestionOption option) {
        return SurveyQuestionOptionDto.builder()
                .id(option.getId())
                .optionNumber(option.getOptionNumber())
                .optionText(option.getOptionText())
                .build();
    }
}