package com.example.live_backend.domain.survey.vitality.service;

import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import com.example.live_backend.domain.survey.vitality.dto.VitalityResultDto;
import com.example.live_backend.domain.survey.vitality.enums.IsolationType;
import com.example.live_backend.domain.survey.vitality.enums.VitalityLevel;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VitalityServiceTest {

    @InjectMocks
    private VitalityService vitalityService;

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Test
    @DisplayName("고립 또는 은둔이 아닐 경우 '정상'을 반환한다")
    void analyzeVitality_Normal() {

        // given
        Long responseId = 1L;
        Map<Integer, Integer> answers = createAnswers(false, false, false, false);
        SurveyResponse mockResponse = createMockResponse(answers);
        when(surveyResponseRepository.findById(responseId)).thenReturn(Optional.of(mockResponse));

        // when
        VitalityResultDto result = vitalityService.analyzeVitality(responseId);

        // then
        assertThat(result.getLevel()).isEqualTo(VitalityLevel.NORMAL);
        assertThat(result.getIsolationType()).isEqualTo(IsolationType.NORMAL);
        assertThat(result.isIsolated()).isFalse();
        assertThat(result.isSecluded()).isFalse();
    }

    @Test
    @DisplayName("고립이지만 은둔이 아닐 경우 '고활력'을 반환한다")
    void analyzeVitality_HighVitality() {

        // given
        Long responseId = 2L;

        // 정서적 고립 O, 장기화 O, 은둔 X
        Map<Integer, Integer> answers = createAnswers(true, true, false, false);
        SurveyResponse mockResponse = createMockResponse(answers);
        when(surveyResponseRepository.findById(responseId)).thenReturn(Optional.of(mockResponse));

        // when
        VitalityResultDto result = vitalityService.analyzeVitality(responseId);

        // then
        assertThat(result.getLevel()).isEqualTo(VitalityLevel.HIGH_VITALITY);
        assertThat(result.getIsolationType()).isEqualTo(IsolationType.EMOTIONAL_ISOLATION);
        assertThat(result.isIsolated()).isTrue();
        assertThat(result.isSecluded()).isFalse();
    }

    @Test
    @DisplayName("고립과 은둔 모두 해당될 경우 '저활력'을 반환한다")
    void analyzeVitality_LowVitality() {

        // given
        Long responseId = 3L;
        // 물리적 고립 O, 장기화 O, 은둔 O
        Map<Integer, Integer> answers = createAnswers(false, true, true, true);
        SurveyResponse mockResponse = createMockResponse(answers);
        when(surveyResponseRepository.findById(responseId)).thenReturn(Optional.of(mockResponse));

        // when
        VitalityResultDto result = vitalityService.analyzeVitality(responseId);

        // then
        assertThat(result.getLevel()).isEqualTo(VitalityLevel.LOW_VITALITY);
        assertThat(result.getIsolationType()).isEqualTo(IsolationType.ISOLATION_AND_SECLUSION);
        assertThat(result.isIsolated()).isTrue();
        assertThat(result.isSecluded()).isTrue();
    }

    @Test
    @DisplayName("필수 문항이 누락된 경우 예외를 발생시킨다")
    void analyzeVitality_MissingQuestion_ThrowsException() {
        // given
        Long responseId = 4L;
        Map<Integer, Integer> answers = createAnswers(false, false, false, false);
        answers.remove(10);
        SurveyResponse mockResponse = createMockResponse(answers);
        when(surveyResponseRepository.findById(responseId)).thenReturn(Optional.of(mockResponse));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> vitalityService.analyzeVitality(responseId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("존재하지 않는 설문 응답 ID일 경우 예외를 발생시킨다")
    void analyzeVitality_NotFound_ThrowsException() {

        // given
        Long responseId = 99L;
        when(surveyResponseRepository.findById(responseId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> vitalityService.analyzeVitality(responseId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SURVEY_RESPONSE_NOT_FOUND);
    }

    private SurveyResponse createMockResponse(Map<Integer, Integer> answerMap) {
        SurveyResponse response = mock(SurveyResponse.class);

        List<SurveyAnswer> answerList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : answerMap.entrySet()) {
            SurveyAnswer answer = mock(SurveyAnswer.class);
            when(answer.getQuestionNumber()).thenReturn(entry.getKey());
            when(answer.getAnswerNumber()).thenReturn(entry.getValue());
            answerList.add(answer);
        }

        when(response.getAnswers()).thenReturn(answerList);
        return response;
    }

    private Map<Integer, Integer> createAnswers(boolean isEmotionallyIsolated, boolean isLongTerm, boolean isPhysicallyIsolated, boolean isSecluded) {
        Map<Integer, Integer> answers = new HashMap<>();

        for (int i = 1; i <= 13; i++) {
            answers.put(i, 1);
        }

        // 정서적 고립
        if (isEmotionallyIsolated) answers.put(1, 4);

        // 물리적 고립
        if (isPhysicallyIsolated) {
            answers.put(7, 1);
            answers.put(8, 1);
        } else {
            answers.put(7, 2);
            answers.put(8, 2);
        }

        // 장기화
        if (isLongTerm) answers.put(9, 4); // 1년 이상 ~ 3년 미만

        // 은둔
        if (isSecluded) {
            answers.put(10, 5); // 보통은 집에 있지만, 인근 편의점 등에 외출한다
            answers.put(11, 4); // 1년 이상 ~ 3년 미만
            answers.put(12, 3); // 일을 하지 않았다
            answers.put(13, 2); // 구직 활동을 하지 않았다
        }
        return answers;
    }
}