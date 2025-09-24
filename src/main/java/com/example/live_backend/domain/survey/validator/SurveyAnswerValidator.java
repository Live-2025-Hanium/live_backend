package com.example.live_backend.domain.survey.validator;

import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SurveyAnswerValidator {

    private static final int MIN_ANSWER_NUMBER = 1;

    public static void validateAnswerFormat(SurveySubmissionDto.SurveyAnswerDto dto, SurveyQuestion question) {
        int maxOptionNumber = getMaxOptionNumber(question);

        if (dto.isSingleChoice()) {
            validateSingleChoice(dto.getAnswerNumber(), dto.getQuestionNumber(), maxOptionNumber);
        } else if (dto.isMultipleChoice()) {
            validateMultipleChoice(dto.getAnswerNumbers(), dto.getQuestionNumber(), maxOptionNumber);
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT,
                String.format("문제 %d번에 대한 답변이 없습니다.", dto.getQuestionNumber()));
        }
    }

    public static void validateQuestionTypeMatch(SurveySubmissionDto.SurveyAnswerDto dto, SurveyQuestion question) {
        boolean isTypeMatch = (question.getQuestionType() == SurveyQuestion.QuestionType.MULTIPLE_CHOICE && dto.isMultipleChoice())
            || (question.getQuestionType() == SurveyQuestion.QuestionType.SINGLE_CHOICE && dto.isSingleChoice());

        if (!isTypeMatch) {
            throw new CustomException(ErrorCode.INVALID_INPUT,
                String.format("문제 %d번의 답변 형식이 올바르지 않습니다.", dto.getQuestionNumber()));
        }
    }

    public static void validateAnswerCompleteness(List<SurveySubmissionDto.SurveyAnswerDto> answers,
                                                  List<SurveyQuestion> activeQuestions,
                                                  Set<Integer> answeredQuestions) {
        if (answers.size() != activeQuestions.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT,
                String.format("설문 문제는 총 %d개입니다. 현재 답변 개수: %d",
                    activeQuestions.size(), answers.size()));
        }

        for (SurveyQuestion question : activeQuestions) {
            if (!answeredQuestions.contains(question.getQuestionNumber())) {
                throw new CustomException(ErrorCode.INVALID_INPUT,
                    String.format("문제 %d번에 대한 답변이 누락되었습니다.", question.getQuestionNumber()));
            }
        }
    }

    private static void validateSingleChoice(Integer answerNumber, Integer questionNumber, int maxOptionNumber) {
        if (answerNumber < MIN_ANSWER_NUMBER || answerNumber > maxOptionNumber) {
            throw new CustomException(ErrorCode.INVALID_INPUT,
                String.format("문제 %d번의 답변 번호는 %d-%d 범위여야 합니다. 입력된 값: %d",
                    questionNumber, MIN_ANSWER_NUMBER, maxOptionNumber, answerNumber));
        }
    }

    private static void validateMultipleChoice(List<Integer> answerNumbers, Integer questionNumber, int maxOptionNumber) {
        for (Integer answerNumber : answerNumbers) {
            if (answerNumber < MIN_ANSWER_NUMBER || answerNumber > maxOptionNumber) {
                throw new CustomException(ErrorCode.INVALID_INPUT,
                    String.format("문제 %d번의 답변 번호는 %d-%d 범위여야 합니다. 입력된 값: %d",
                        questionNumber, MIN_ANSWER_NUMBER, maxOptionNumber, answerNumber));
            }
        }
    }

    private static int getMaxOptionNumber(SurveyQuestion question) {
        return question.getOptions().stream()
            .mapToInt(SurveyQuestionOption::getOptionNumber)
            .max()
            .orElse(0);
    }
}