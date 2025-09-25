package com.example.live_backend.domain.survey.factory;

import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SurveyAnswerFactory {

    public static void createAndAddAnswers(SurveyResponse surveyResponse,
                                          SurveySubmissionDto.SurveyAnswerDto dto,
                                          SurveyQuestion question) {
        if (dto.isMultipleChoice()) {
            createMultipleAnswers(surveyResponse, dto, question);
        } else {
            createSingleAnswer(surveyResponse, dto, question);
        }
    }

    private static void createMultipleAnswers(SurveyResponse surveyResponse,
                                             SurveySubmissionDto.SurveyAnswerDto dto,
                                             SurveyQuestion question) {
        for (Integer optionNumber : dto.getAnswerNumbers()) {
            SurveyQuestionOption selectedOption = findOption(question, optionNumber);
            SurveyAnswer answer = buildAnswer(question, selectedOption, optionNumber);
            surveyResponse.addAnswer(answer);
        }
    }

    private static void createSingleAnswer(SurveyResponse surveyResponse,
                                          SurveySubmissionDto.SurveyAnswerDto dto,
                                          SurveyQuestion question) {
        SurveyQuestionOption selectedOption = findOption(question, dto.getAnswerNumber());
        SurveyAnswer answer = buildAnswer(question, selectedOption, dto.getAnswerNumber());
        surveyResponse.addAnswer(answer);
    }

    private static SurveyQuestionOption findOption(SurveyQuestion question, Integer optionNumber) {
        return question.getOptions().stream()
            .filter(opt -> opt.getOptionNumber().equals(optionNumber))
            .findFirst()
            .orElse(null);
    }

    private static SurveyAnswer buildAnswer(SurveyQuestion question,
                                           SurveyQuestionOption selectedOption,
                                           Integer numberAnswer) {
        return SurveyAnswer.builder()
            .surveyQuestion(question)
            .selectedOption(selectedOption)
            .numberAnswer(numberAnswer)
            .build();
    }
}