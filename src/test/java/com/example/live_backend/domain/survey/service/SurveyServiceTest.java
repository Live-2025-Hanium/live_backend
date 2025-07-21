package com.example.live_backend.domain.survey.service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.domain.memeber.util.UserUtil;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("설문 서비스 테스트")
class SurveyServiceTest {

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private SurveyService surveyService;

    private SurveySubmissionDto validRequest;
    private SurveyResponse mockSurveyResponse;
    private final Long MOCK_USER_ID = 1L;

    @BeforeEach
    void initFixtures() {

        List<SurveySubmissionDto.SurveyAnswerDto> answers = Arrays.asList(
            new SurveySubmissionDto.SurveyAnswerDto(1, 3),
            new SurveySubmissionDto.SurveyAnswerDto(2, 2),
            new SurveySubmissionDto.SurveyAnswerDto(3, 4),
            new SurveySubmissionDto.SurveyAnswerDto(4, 1),
            new SurveySubmissionDto.SurveyAnswerDto(5, 5),
            new SurveySubmissionDto.SurveyAnswerDto(6, 1),
            new SurveySubmissionDto.SurveyAnswerDto(7, 2),
            new SurveySubmissionDto.SurveyAnswerDto(8, 3),
            new SurveySubmissionDto.SurveyAnswerDto(9, 4),
            new SurveySubmissionDto.SurveyAnswerDto(10, 5),
            new SurveySubmissionDto.SurveyAnswerDto(11, 1),
            new SurveySubmissionDto.SurveyAnswerDto(12, 2),
            new SurveySubmissionDto.SurveyAnswerDto(13, 3),
            new SurveySubmissionDto.SurveyAnswerDto(14, 4),
            new SurveySubmissionDto.SurveyAnswerDto(15, 5)
        );
        validRequest = new SurveySubmissionDto(answers);

        mockSurveyResponse = SurveyResponse.builder()
            .userId(MOCK_USER_ID)
            .build();

        setField(mockSurveyResponse, "id", 100L);
        setField(mockSurveyResponse, "createdAt", LocalDateTime.now());
        answers.forEach(dto -> {
            SurveyAnswer answer = SurveyAnswer.builder()
                .questionNumber(dto.getQuestionNumber())
                .answerNumber(dto.getAnswerNumber())
                .build();
            mockSurveyResponse.addAnswer(answer);
        });
    }

    @Nested
    @DisplayName("설문 제출 기능")
    class SubmitTests {

        @BeforeEach
        void stubUserId() {
            // 매번 submit 시 현재 사용자 ID는 MOCK_USER_ID로
            given(userUtil.getCurrentUserId()).willReturn(MOCK_USER_ID);
        }

        @Test
        @DisplayName("정상적인 설문 제출 - 성공")
        void submitSurvey_Success() {
            // Given
            given(surveyResponseRepository.save(any(SurveyResponse.class)))
                .willReturn(mockSurveyResponse);

            // When
            SurveySubmissionResponseDto result = surveyService.submitSurvey(validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getResponseId()).isEqualTo(100L);
            assertThat(result.getTotalAnswers()).isEqualTo(15);
            assertThat(result.getSubmittedAt()).isNotNull();

            verify(userUtil).getCurrentUserId();
            verify(surveyResponseRepository).save(any(SurveyResponse.class));
        }

        @Test
        @DisplayName("설문 제출 - 답변 개수 부족 시 예외")
        void submitSurvey_InsufficientAnswers_ThrowsException() {
            // Given
            List<SurveySubmissionDto.SurveyAnswerDto> insufficient = validRequest.getAnswers().subList(0, 14);
            SurveySubmissionDto req = new SurveySubmissionDto(insufficient);

            // When & Then
            Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

            assertThat(t)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("설문 문제는 총 15개입니다. 현재 답변 개수: 14");
            assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("설문 제출 - 문제 번호 중복 시 예외")
        void submitSurvey_DuplicateQuestionNumber_ThrowsException() {
            // Given
            List<SurveySubmissionDto.SurveyAnswerDto> dup = Arrays.asList(
                new SurveySubmissionDto.SurveyAnswerDto(1, 3),
                new SurveySubmissionDto.SurveyAnswerDto(1, 2),
                new SurveySubmissionDto.SurveyAnswerDto(3, 4),
                new SurveySubmissionDto.SurveyAnswerDto(4, 1),
                new SurveySubmissionDto.SurveyAnswerDto(5, 5),
                new SurveySubmissionDto.SurveyAnswerDto(6, 1),
                new SurveySubmissionDto.SurveyAnswerDto(7, 2),
                new SurveySubmissionDto.SurveyAnswerDto(8, 3),
                new SurveySubmissionDto.SurveyAnswerDto(9, 4),
                new SurveySubmissionDto.SurveyAnswerDto(10, 5),
                new SurveySubmissionDto.SurveyAnswerDto(11, 1),
                new SurveySubmissionDto.SurveyAnswerDto(12, 2),
                new SurveySubmissionDto.SurveyAnswerDto(13, 3),
                new SurveySubmissionDto.SurveyAnswerDto(14, 4),
                new SurveySubmissionDto.SurveyAnswerDto(15, 5)
            );
            SurveySubmissionDto req = new SurveySubmissionDto(dup);

            // When & Then
            Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

            assertThat(t)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("문제 번호 1가 중복되었습니다");
            assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("설문 제출 - 문제 번호 범위 초과 시 예외 (16번)")
        void submitSurvey_QuestionNumberOutOfRange_ThrowsException() {
            // Given - 2번 문제를 빼고 16번을 추가하여 15개 답변 유지
            List<SurveySubmissionDto.SurveyAnswerDto> missing = Arrays.asList(
                new SurveySubmissionDto.SurveyAnswerDto(1, 3),
                new SurveySubmissionDto.SurveyAnswerDto(3, 4),
                new SurveySubmissionDto.SurveyAnswerDto(4, 1),
                new SurveySubmissionDto.SurveyAnswerDto(5, 5),
                new SurveySubmissionDto.SurveyAnswerDto(6, 1),
                new SurveySubmissionDto.SurveyAnswerDto(7, 2),
                new SurveySubmissionDto.SurveyAnswerDto(8, 3),
                new SurveySubmissionDto.SurveyAnswerDto(9, 4),
                new SurveySubmissionDto.SurveyAnswerDto(10, 5),
                new SurveySubmissionDto.SurveyAnswerDto(11, 1),
                new SurveySubmissionDto.SurveyAnswerDto(12, 2),
                new SurveySubmissionDto.SurveyAnswerDto(13, 3),
                new SurveySubmissionDto.SurveyAnswerDto(14, 4),
                new SurveySubmissionDto.SurveyAnswerDto(15, 5),
                new SurveySubmissionDto.SurveyAnswerDto(16, 1)
            );
            SurveySubmissionDto req = new SurveySubmissionDto(missing);

            // When & Then
            Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

            assertThat(t)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("문제 번호는 1-15 범위여야 합니다. 입력된 값: 16");
            assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("설문 제출 - 잘못된 문제 번호 범위 시 예외")
        void submitSurvey_InvalidQuestionNumber_ThrowsException() {
            // Given
            List<SurveySubmissionDto.SurveyAnswerDto> outOfRange = Arrays.asList(
                new SurveySubmissionDto.SurveyAnswerDto(1, 3),
                new SurveySubmissionDto.SurveyAnswerDto(2, 2),
                new SurveySubmissionDto.SurveyAnswerDto(3, 4),
                new SurveySubmissionDto.SurveyAnswerDto(4, 1),
                new SurveySubmissionDto.SurveyAnswerDto(5, 5),
                new SurveySubmissionDto.SurveyAnswerDto(6, 1),
                new SurveySubmissionDto.SurveyAnswerDto(7, 2),
                new SurveySubmissionDto.SurveyAnswerDto(8, 3),
                new SurveySubmissionDto.SurveyAnswerDto(9, 4),
                new SurveySubmissionDto.SurveyAnswerDto(10, 5),
                new SurveySubmissionDto.SurveyAnswerDto(11, 1),
                new SurveySubmissionDto.SurveyAnswerDto(12, 2),
                new SurveySubmissionDto.SurveyAnswerDto(13, 3),
                new SurveySubmissionDto.SurveyAnswerDto(14, 4),
                new SurveySubmissionDto.SurveyAnswerDto(16, 5)
            );
            SurveySubmissionDto req = new SurveySubmissionDto(outOfRange);

            // When & Then
            Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

            assertThat(t)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("문제 번호는 1-15 범위여야 합니다. 입력된 값: 16");
            assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("설문 제출 - 잘못된 답변 번호 범위 시 예외")
        void submitSurvey_InvalidAnswerNumber_ThrowsException() {
            // Given
            List<SurveySubmissionDto.SurveyAnswerDto> badAnswer = Arrays.asList(
                new SurveySubmissionDto.SurveyAnswerDto(1, 3),
                new SurveySubmissionDto.SurveyAnswerDto(2, 2),
                new SurveySubmissionDto.SurveyAnswerDto(3, 4),
                new SurveySubmissionDto.SurveyAnswerDto(4, 1),
                new SurveySubmissionDto.SurveyAnswerDto(5, 5),
                new SurveySubmissionDto.SurveyAnswerDto(6, 1),
                new SurveySubmissionDto.SurveyAnswerDto(7, 2),
                new SurveySubmissionDto.SurveyAnswerDto(8, 3),
                new SurveySubmissionDto.SurveyAnswerDto(9, 4),
                new SurveySubmissionDto.SurveyAnswerDto(10, 5),
                new SurveySubmissionDto.SurveyAnswerDto(11, 1),
                new SurveySubmissionDto.SurveyAnswerDto(12, 2),
                new SurveySubmissionDto.SurveyAnswerDto(13, 3),
                new SurveySubmissionDto.SurveyAnswerDto(14, 4),
                new SurveySubmissionDto.SurveyAnswerDto(15, 16)
            );
            SurveySubmissionDto req = new SurveySubmissionDto(badAnswer);

            // When & Then
            Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

            assertThat(t)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("답변 번호는 1-15 범위여야 합니다. 입력된 값: 16");
            assertThat(((CustomException) t).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("Spring Security 인증 테스트 - 사용자 ID 추출")
        void submitSurvey_SecurityContextUserId_Success() {
            // Given
            Long expectedId = 999L;
            given(userUtil.getCurrentUserId()).willReturn(expectedId);
            given(surveyResponseRepository.save(any(SurveyResponse.class)))
                .willAnswer(invocation -> {
                    SurveyResponse saved = invocation.getArgument(0);
                    assertThat(saved.getUserId()).isEqualTo(expectedId);
                    setField(saved, "id", 200L);
                    setField(saved, "createdAt", LocalDateTime.now());
                    return saved;
                });

            // When
            SurveySubmissionResponseDto result = surveyService.submitSurvey(validRequest);

            // Then
            assertThat(result.getResponseId()).isEqualTo(200L);
            verify(userUtil).getCurrentUserId();
            verify(surveyResponseRepository).save(any(SurveyResponse.class));
        }
    }

    @Nested
    @DisplayName("조회 기능")
    class QueryTests {

        @Test
        @DisplayName("사용자별 설문 응답 조회 - 성공")
        void getUserSurveyResponses_Success() {
            // Given
            List<SurveyResponse> mockList = List.of(mockSurveyResponse);
            given(surveyResponseRepository.findByUserIdOrderByCreatedAtDesc(MOCK_USER_ID))
                .willReturn(mockList);
            given(surveyResponseRepository.countByUserId(MOCK_USER_ID))
                .willReturn(1L);

            // When
            SurveyResponseListDto.UserSurveyResponseListDto result =
                surveyService.getUserSurveyResponses(MOCK_USER_ID);

            // Then
            assertThat(result.getUserId()).isEqualTo(MOCK_USER_ID);
            assertThat(result.getTotalResponseCount()).isEqualTo(1L);
            assertThat(result.getResponses()).hasSize(1);
            assertThat(result.getResponses().get(0).getUserId()).isEqualTo(MOCK_USER_ID);
        assertThat(result.getResponses().get(0).getAnswers()).hasSize(15);

            verify(surveyResponseRepository)
                .findByUserIdOrderByCreatedAtDesc(MOCK_USER_ID);
            verify(surveyResponseRepository)
                .countByUserId(MOCK_USER_ID);
        }

        @Test
        @DisplayName("기간별 설문 응답 조회 - 성공")
        void getSurveyResponsesByPeriod_Success() {
            // Given
            List<SurveyResponse> mockList = List.of(mockSurveyResponse);
            given(surveyResponseRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(mockList);

            // When
            List<SurveyResponseListDto> result = surveyService.getSurveyResponsesByPeriod(
                LocalDateTime.now().toLocalDate().minusDays(1),
                LocalDateTime.now().toLocalDate()
            );

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(MOCK_USER_ID);
        assertThat(result.get(0).getAnswers()).hasSize(15);

            verify(surveyResponseRepository)
                .findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }

    // 리플렉션으로 private 필드 세팅
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            try {
                var field = target.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception ex) {
                throw new RuntimeException("필드 설정 실패: " + fieldName, ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }
}