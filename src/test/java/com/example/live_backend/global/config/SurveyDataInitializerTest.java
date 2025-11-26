package com.example.live_backend.global.config;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.repository.SurveyQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SurveyDataInitializer 테스트")
class SurveyDataInitializerTest {

    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @InjectMocks
    private SurveyDataInitializer surveyDataInitializer;

    @Captor
    private ArgumentCaptor<SurveyQuestion> questionCaptor;

    @Nested
    @DisplayName("초기화 실행 조건 테스트")
    class InitializationConditionTests {

        @Test
        @DisplayName("데이터가 없을 때 초기화가 실행된다")
        void whenNoDataExists_thenInitializationRuns() throws Exception {
            // given
            given(surveyQuestionRepository.count()).willReturn(0L);

            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(any(SurveyQuestion.class));
        }

        @Test
        @DisplayName("데이터가 이미 있을 때 초기화를 건너뛴다")
        void whenDataExists_thenInitializationSkipped() throws Exception {
            // given
            given(surveyQuestionRepository.count()).willReturn(1L);

            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, never()).save(any(SurveyQuestion.class));
        }

        @Test
        @DisplayName("데이터가 여러 개 있을 때도 초기화를 건너뛴다")
        void whenMultipleDataExists_thenInitializationSkipped() throws Exception {
            // given
            given(surveyQuestionRepository.count()).willReturn(13L);

            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, never()).save(any(SurveyQuestion.class));
        }
    }

    @Nested
    @DisplayName("문항 삽입 검증 테스트")
    class QuestionInsertionTests {

        @BeforeEach
        void setUp() {
            given(surveyQuestionRepository.count()).willReturn(0L);
        }

        @Test
        @DisplayName("13개의 설문 문항이 삽입된다")
        void shouldInsert13Questions() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            List<SurveyQuestion> savedQuestions = questionCaptor.getAllValues();
            assertThat(savedQuestions).hasSize(13);
        }

        @Test
        @DisplayName("문항 번호가 1부터 13까지 순서대로 삽입된다")
        void shouldInsertQuestionsInOrder() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            List<SurveyQuestion> savedQuestions = questionCaptor.getAllValues();

            for (int i = 0; i < 13; i++) {
                assertThat(savedQuestions.get(i).getQuestionNumber()).isEqualTo(i + 1);
            }
        }

        @Test
        @DisplayName("각 문항에 올바른 개수의 옵션이 포함된다")
        void shouldHaveCorrectOptionCounts() throws Exception {
            // given
            // 1~4번: 4개, 5~9번: 5개, 10번: 7개, 11번: 5개, 12번: 3개, 13번: 2개
            int[] expectedOptionCounts = {4, 4, 4, 4, 5, 5, 5, 5, 5, 7, 5, 3, 2};

            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            List<SurveyQuestion> savedQuestions = questionCaptor.getAllValues();

            for (int i = 0; i < 13; i++) {
                assertThat(savedQuestions.get(i).getOptions())
                        .hasSize(expectedOptionCounts[i])
                        .as("문항 %d의 옵션 개수", i + 1);
            }
        }

        @Test
        @DisplayName("모든 문항이 필수(isRequired=true)로 설정된다")
        void allQuestionsShouldBeRequired() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            List<SurveyQuestion> savedQuestions = questionCaptor.getAllValues();

            assertThat(savedQuestions)
                    .allMatch(SurveyQuestion::isRequired);
        }

        @Test
        @DisplayName("모든 문항이 활성화(isActive=true) 상태로 설정된다")
        void allQuestionsShouldBeActive() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            List<SurveyQuestion> savedQuestions = questionCaptor.getAllValues();

            assertThat(savedQuestions)
                    .allMatch(SurveyQuestion::isActive);
        }
    }

    @Nested
    @DisplayName("문항 내용 검증 테스트")
    class QuestionContentTests {

        @BeforeEach
        void setUp() {
            given(surveyQuestionRepository.count()).willReturn(0L);
        }

        @Test
        @DisplayName("1번 문항이 올바른 텍스트를 가진다")
        void question1ShouldHaveCorrectText() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            SurveyQuestion question1 = questionCaptor.getAllValues().get(0);

            assertThat(question1.getQuestionText())
                    .contains("중요하거나 어려운 일이 있을 때 주로 누구에게 조언을 구하나요?");
        }

        @Test
        @DisplayName("1~4번 문항의 마지막 옵션이 '없음'이다")
        void questions1to4ShouldHaveNoOptionAsLast() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            List<SurveyQuestion> savedQuestions = questionCaptor.getAllValues();

            for (int i = 0; i < 4; i++) {
                SurveyQuestion question = savedQuestions.get(i);
                String lastOptionText = question.getOptions()
                        .get(question.getOptions().size() - 1)
                        .getOptionText();

                assertThat(lastOptionText)
                        .isEqualTo("없음")
                        .as("문항 %d의 마지막 옵션", i + 1);
            }
        }

        @Test
        @DisplayName("10번 문항(외출 빈도)이 7개의 옵션을 가진다")
        void question10ShouldHave7Options() throws Exception {
            // when
            surveyDataInitializer.run(null);

            // then
            verify(surveyQuestionRepository, times(13)).save(questionCaptor.capture());
            SurveyQuestion question10 = questionCaptor.getAllValues().get(9);

            assertThat(question10.getOptions()).hasSize(7);
            assertThat(question10.getQuestionText()).contains("외출");
        }
    }
}