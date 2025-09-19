package com.example.live_backend.domain.survey.vitality.service;

import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import com.example.live_backend.domain.survey.vitality.dto.VitalityResultDto;
import com.example.live_backend.domain.survey.vitality.enums.IsolationType;
import com.example.live_backend.domain.survey.vitality.enums.VitalityLevel;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        boolean isSecluded = isSecludedYouth(response);

        // 5. 활력 판단
        VitalityLevel vitalityLevel;
        IsolationType isolationType;
        String description;

        if (!isIsolated) {
            vitalityLevel = VitalityLevel.NORMAL;
            isolationType = IsolationType.NORMAL;
            description = "고립 또는 은둔 상태가 아닙니다.";
        } else if (isIsolated && !isSecluded) {
            vitalityLevel = VitalityLevel.HIGH_VITALITY;
            isolationType = getIsolationTypeEnum(emotionalIsolation, physicalIsolation);
            description = "고립 상태이지만 은둔 상태는 아니므로 고활력으로 판단됩니다.";
        } else {
            vitalityLevel = VitalityLevel.LOW_VITALITY;
            isolationType = IsolationType.ISOLATION_AND_SECLUSION;
            description = "고립과 은둔 상태가 모두 해당되므로 저활력으로 판단됩니다.";
        }

        return VitalityResultDto.builder()
                .level(vitalityLevel)
                .isolationType(isolationType)
                .isIsolated(isIsolated)
                .isSecluded(isSecluded)
                .description(description)
                .build();
    }

    // 정서적 고립: 1~4번 문항 중 하나라도 4번(없음) 선택
    private boolean isEmotionallyIsolated(SurveyResponse response) {
        for (int questionNum = 1; questionNum <= 4; questionNum++) {
            int answer = getAnswerNumber(response, questionNum);
            if (answer == 4) { // 4번: 없음
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
    private boolean isSecludedYouth(SurveyResponse response) {
        int answer10 = getAnswerNumber(response, 10);
        int answer11 = getAnswerNumber(response, 11);
        int answer12 = getAnswerNumber(response, 12);
        int answer13 = getAnswerNumber(response, 13);

        return (answer10 >= 4 && answer10 <= 7) && // 10번: 4~7번
                (answer11 >= 3 && answer11 <= 5) && // 11번: 3~5번
                (answer12 == 3) &&                  // 12번: 3번
                (answer13 == 2);                    // 13번: 2번
    }

    // 고립 유형 enum 반환
    private IsolationType getIsolationTypeEnum(boolean emotional, boolean physical) {
        if (emotional && physical) {
            return IsolationType.EMOTIONAL_AND_PHYSICAL_ISOLATION;
        } else if (emotional) {
            return IsolationType.EMOTIONAL_ISOLATION;
        } else if (physical) {
            return IsolationType.PHYSICAL_ISOLATION;
        }
        return IsolationType.NORMAL;
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

    // 특정 질문 번호의 답변 가져오기
    private int getAnswerNumber(SurveyResponse response, int questionNumber) {
        return response.getAnswers().stream()
                .filter(a -> a.getQuestionNumber() == questionNumber)
                .map(SurveyAnswer::getAnswerNumber)
                .findFirst()
                .orElse(0);
    }
}