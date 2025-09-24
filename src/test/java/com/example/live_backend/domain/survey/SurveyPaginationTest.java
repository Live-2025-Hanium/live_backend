package com.example.live_backend.domain.survey;

import com.example.live_backend.domain.survey.dto.response.SurveyPageResponse;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.service.SurveyQuestionService;
import com.example.live_backend.global.error.exception.CustomException;
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