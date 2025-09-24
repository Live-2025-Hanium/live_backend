package com.example.live_backend.domain.survey.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyPageResponse {

	@JsonProperty("questions")
	private final List<SurveyQuestionDto> questions;

	@JsonProperty("currentPage")
	private final int currentPage;

	@JsonProperty("totalPages")
	private final int totalPages;

	@JsonProperty("totalQuestions")
	private final int totalQuestions;

	@JsonProperty("questionsPerPage")
	private final int questionsPerPage;

	@JsonProperty("hasNext")
	private final boolean hasNext;

	@JsonProperty("hasPrevious")
	private final boolean hasPrevious;

	@JsonProperty("progressPercentage")
	private final double progressPercentage;

	public static SurveyPageResponse of(
		List<SurveyQuestionDto> questions,
		int currentPage,
		int totalPages,
		int totalQuestions,
		int questionsPerPage
	) {
		int answeredQuestions = currentPage * questionsPerPage;
		double progress = Math.min(100.0, ((double) answeredQuestions / totalQuestions) * 100);

		return SurveyPageResponse.builder()
			.questions(questions)
			.currentPage(currentPage)
			.totalPages(totalPages)
			.totalQuestions(totalQuestions)
			.questionsPerPage(questionsPerPage)
			.hasNext(currentPage < totalPages)
			.hasPrevious(currentPage > 1)
			.progressPercentage(progress)
			.build();
	}
}