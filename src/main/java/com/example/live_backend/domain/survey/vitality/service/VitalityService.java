package com.example.live_backend.domain.survey.vitality.service;

import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import com.example.live_backend.domain.survey.vitality.dto.VitalityResultDto;
import com.example.live_backend.domain.survey.vitality.enums.IsolationAndSeclusionType;
import com.example.live_backend.domain.survey.vitality.enums.VitalityLevel;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VitalityService {

    private final SurveyResponseRepository surveyResponseRepository;

    @Transactional(readOnly = true)
    public VitalityResultDto analyzeVitality(Long surveyResponseId) {
        SurveyResponse response = surveyResponseRepository.findById(surveyResponseId)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_RESPONSE_NOT_FOUND));

        // 필수 문항 존재 여부 확인 (1~13번)
        if (!hasAllRequiredQuestions(response)) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "활력 분석에 필요한 문항이 누락되었습니다.");
        }

        // 1. 정서적 고립 판단: 1~4번 문항 중 하나라도 4번(없음) 선택
        boolean emotionalIsolation = isEmotionallyIsolated(response);

        // 2. 물리적 고립 판단: 7,8번 문항이 모두 1번(전혀, 거의 없다) 선택
        boolean physicalIsolation = isPhysicallyIsolated(response);

        // 3. 고립 청년 판단: (정서적 고립 OR 물리적 고립) + 9번 문항 6개월 이상 지속
        boolean isIsolated = (emotionalIsolation || physicalIsolation) && isDurationLongTerm(response, 9);

        // 4. 은둔 청년 판단: 복합 조건
        boolean isSecluded = isSecluded(response);

        VitalityLevel vitalityLevel;
        IsolationAndSeclusionType isolationAndSeclusionType;

        if (!isIsolated) {
            // 고립이 아니라면 -> 정상
            vitalityLevel = VitalityLevel.NORMAL;
            isolationAndSeclusionType = IsolationAndSeclusionType.NORMAL;
        } else {
            // 고립 상태일 때, 은둔 여부에 따라 활력 레벨 결정
            vitalityLevel = isSecluded ? VitalityLevel.LOW_VITALITY : VitalityLevel.HIGH_VITALITY;

            isolationAndSeclusionType = determineIsolationAndSeclusionType(emotionalIsolation, physicalIsolation, isSecluded);
        }

        return VitalityResultDto.builder()
                .vitalityLevel(vitalityLevel)
                .isolationAndSeclusionType(isolationAndSeclusionType)
                .isIsolated(isIsolated)
                .isSecluded(isSecluded)
                .description(isolationAndSeclusionType.getDescription())
                .build();
    }

    private IsolationAndSeclusionType determineIsolationAndSeclusionType(
            boolean emotionalIsolation, boolean physicalIsolation, boolean isSecluded) {

        if (emotionalIsolation && physicalIsolation) {
            return isSecluded
                    ? IsolationAndSeclusionType.EMOTIONAL_ISOLATION_AND_PHYSICAL_ISOLATION_AND_SECLUSION
                    : IsolationAndSeclusionType.EMOTIONAL_AND_PHYSICAL_ISOLATION;
        } else if (emotionalIsolation) {
            return isSecluded
                    ? IsolationAndSeclusionType.EMOTIONAL_ISOLATION_AND_SECLUSION
                    : IsolationAndSeclusionType.EMOTIONAL_ISOLATION;
        } else if (physicalIsolation) {
            return isSecluded
                    ? IsolationAndSeclusionType.PHYSICAL_ISOLATION_AND_SECLUSION
                    : IsolationAndSeclusionType.PHYSICAL_ISOLATION;
        }

        // 여기에 도달하면 안 됨 (isIsolated가 true인 경우에만 호출되므로)
        return IsolationAndSeclusionType.NORMAL;
    }

    // 정서적 고립: 1~4번 문항 중 하나라도 4번(없음)만 선택
    private boolean isEmotionallyIsolated(SurveyResponse response) {
        for (int questionNum = 1; questionNum <= 4; questionNum++) {
            List<Integer> answers = getAnswerNumbers(response, questionNum);
            // 다중 선택 문항에서 "없음"(4번)만 선택했는지 확인
            if (answers.size() == 1 && answers.contains(4)) {
                return true;
            }
        }
        return false;
    }

    // 물리적 고립: 7,8번 문항이 모두 1번(전혀, 거의 없다) 선택
    private boolean isPhysicallyIsolated(SurveyResponse response) {
        int answer7 = getAnswerNumber(response, 7);
        int answer8 = getAnswerNumber(response, 8);
        return answer7 == 1 && answer8 == 1;
    }

    // 특정 문항이 6개월 이상 지속인지 확인 (3번~5번)
    private boolean isDurationLongTerm(SurveyResponse response, int questionNumber) {
        int answer = getAnswerNumber(response, questionNumber);
        return answer >= 3 && answer <= 5; // 3번(6개월~1년), 4번(1년~3년), 5번(3년 이상)
    }

    // 은둔 청년 판단: 10번(4~7번) + 11번(3~5번) + 12번(3번) + 13번(2번)
    private boolean isSecluded(SurveyResponse response) {
        int answer10 = getAnswerNumber(response, 10);
        int answer11 = getAnswerNumber(response, 11);
        int answer12 = getAnswerNumber(response, 12);
        int answer13 = getAnswerNumber(response, 13);

        return (answer10 >= 4 && answer10 <= 7) && // 10번: 4~7번
                (answer11 >= 3 && answer11 <= 5) && // 11번: 3~5번
                (answer12 == 3) &&                  // 12번: 3번
                (answer13 == 2);                    // 13번: 2번
    }

    // 필수 문항 존재 여부 확인 (1~13번)
    private boolean hasAllRequiredQuestions(SurveyResponse response) {
        for (int i = 1; i <= 13; i++) {
            if (!hasQuestion(response, i)) {
                return false;
            }
        }
        return true;
    }

    // 특정 질문 번호 존재 여부 확인
    private boolean hasQuestion(SurveyResponse response, int questionNumber) {
        return response.getAnswers().stream()
                .anyMatch(a -> a.getQuestionNumber() == questionNumber);
    }

    // 특정 질문 번호의 답변 가져오기 (단일 선택용)
    private int getAnswerNumber(SurveyResponse response, int questionNumber) {
        return response.getAnswers().stream()
                .filter(a -> a.getQuestionNumber() == questionNumber)
                .map(SurveyAnswer::getAnswerNumber)
                .findFirst()
                .orElse(0);
    }

    // 특정 질문 번호의 답변들 가져오기 (다중 선택용)
    private List<Integer> getAnswerNumbers(SurveyResponse response, int questionNumber) {
        return response.getAnswers().stream()
                .filter(a -> a.getQuestionNumber() == questionNumber)
                .map(SurveyAnswer::getAnswerNumber)
                .collect(Collectors.toList());
    }
}