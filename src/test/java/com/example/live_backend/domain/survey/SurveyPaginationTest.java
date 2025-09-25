package com.example.live_backend.domain.survey;

import com.example.live_backend.domain.survey.dto.response.SurveyPageResponse;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.domain.survey.repository.SurveyQuestionRepository;
import com.example.live_backend.domain.survey.repository.SurveyQuestionOptionRepository;
import com.example.live_backend.domain.survey.service.SurveyQuestionService;
import com.example.live_backend.global.error.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SurveyPaginationTest {

	@Autowired
	private SurveyQuestionService surveyQuestionService;

	@Autowired
	private SurveyQuestionRepository surveyQuestionRepository;

	@Autowired
	private SurveyQuestionOptionRepository surveyQuestionOptionRepository;

	@BeforeEach
	void setUp() {
		// 기존 데이터 삭제
		surveyQuestionOptionRepository.deleteAll();
		surveyQuestionRepository.deleteAll();

		// 13개의 테스트 문항 생성
		for (int i = 1; i <= 13; i++) {
			SurveyQuestion.QuestionType type = i <= 4 ? SurveyQuestion.QuestionType.MULTIPLE_CHOICE : SurveyQuestion.QuestionType.SINGLE_CHOICE;

			SurveyQuestion question = SurveyQuestion.builder()
					.questionNumber(i)
					.questionText("테스트 질문 " + i)
					.questionType(type)
					.isRequired(true)
					.isActive(true)
					.build();

			question = surveyQuestionRepository.save(question);

			// 각 질문에 대한 옵션 생성
			int optionCount = i <= 4 ? 4 : 5; // 1-4번은 4개 옵션, 나머지는 5개 옵션
			for (int j = 1; j <= optionCount; j++) {
				SurveyQuestionOption option = SurveyQuestionOption.builder()
						.surveyQuestion(question)
						.optionNumber(j)
						.optionText("옵션 " + j)
						.isActive(true)
						.build();
				surveyQuestionOptionRepository.save(option);
			}
		}
	}

	@Test
	@DisplayName("첫 페이지 조회 - 5개 문항 반환")
	void getFirstPage() {
		SurveyPageResponse response = surveyQuestionService.getQuestionsByPage(1);

		assertThat(response).isNotNull();
		assertThat(response.getCurrentPage()).isEqualTo(1);
		assertThat(response.getQuestions().size()).isLessThanOrEqualTo(5);
		assertThat(response.isHasPrevious()).isFalse();
		assertThat(response.getQuestionsPerPage()).isEqualTo(5);
	}

	@Test
	@DisplayName("두 번째 페이지 조회")
	void getSecondPage() {
		SurveyPageResponse response = surveyQuestionService.getQuestionsByPage(2);

		assertThat(response).isNotNull();
		assertThat(response.getCurrentPage()).isEqualTo(2);
		assertThat(response.getQuestions().size()).isLessThanOrEqualTo(5);
		assertThat(response.isHasPrevious()).isTrue();
	}

	@Test
	@DisplayName("마지막 페이지 조회")
	void getLastPage() {
		SurveyPageResponse response = surveyQuestionService.getQuestionsByPage(3);

		assertThat(response).isNotNull();
		assertThat(response.getCurrentPage()).isEqualTo(3);
		assertThat(response.isHasNext()).isFalse();
		assertThat(response.isHasPrevious()).isTrue();
	}

	@Test
	@DisplayName("진행률 계산 확인")
	void checkProgressPercentage() {
		SurveyPageResponse firstPage = surveyQuestionService.getQuestionsByPage(1);
		SurveyPageResponse secondPage = surveyQuestionService.getQuestionsByPage(2);
		SurveyPageResponse thirdPage = surveyQuestionService.getQuestionsByPage(3);

		assertThat(firstPage.getProgressPercentage()).isGreaterThan(0);
		assertThat(secondPage.getProgressPercentage()).isGreaterThan(firstPage.getProgressPercentage());
		assertThat(thirdPage.getProgressPercentage()).isLessThanOrEqualTo(100);
	}

	@Test
	@DisplayName("잘못된 페이지 번호 요청시 예외 발생")
	void invalidPageNumber() {
		assertThatThrownBy(() -> surveyQuestionService.getQuestionsByPage(0))
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("페이지 번호는 1 이상이어야 합니다");

		assertThatThrownBy(() -> surveyQuestionService.getQuestionsByPage(-1))
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("페이지 번호는 1 이상이어야 합니다");
	}

	@Test
	@DisplayName("페이지 범위 초과시 예외 발생")
	void pageOutOfRange() {
		assertThatThrownBy(() -> surveyQuestionService.getQuestionsByPage(100))
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("페이지 번호가 범위를 초과했습니다");
	}
}