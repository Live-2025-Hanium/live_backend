package com.example.live_backend.domain.survey.factory;

import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SurveyQuestionFactory {

    public static SurveyQuestion createQuestion(CreateQuestionRequest request) {
        return SurveyQuestion.builder()
            .questionNumber(request.getQuestionNumber())
            .questionText(request.getQuestionText())
            .questionType(request.getQuestionType())
            .isRequired(request.isRequired())
            .isActive(request.isActive())
            .build();
    }

    public static void addOptionsToQuestion(SurveyQuestion question,
                                           List<CreateQuestionRequest.CreateOptionRequest> optionRequests) {
        if (optionRequests == null || optionRequests.isEmpty()) {
            return;
        }

        for (CreateQuestionRequest.CreateOptionRequest optionRequest : optionRequests) {
            SurveyQuestionOption option = createOption(question, optionRequest);
            question.addOption(option);
        }
    }

    private static SurveyQuestionOption createOption(SurveyQuestion question,
                                                    CreateQuestionRequest.CreateOptionRequest optionRequest) {
        return SurveyQuestionOption.builder()
            .surveyQuestion(question)
            .optionNumber(optionRequest.getOptionNumber())
            .optionText(optionRequest.getOptionText())
            .isActive(optionRequest.isActive())
            .build();
    }
}