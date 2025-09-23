package com.example.live_backend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveySubmissionDto {

    @Schema(description = "설문 응답 목록")
    @NotEmpty(message = "설문 응답은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<SurveyAnswerDto> answers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyAnswerDto {

        @Schema(description = "문제 번호", example = "1")
        @NotNull(message = "문제 번호는 필수입니다.")
        private Integer questionNumber;

        @Schema(description = "단일 선택 답변 번호 (단일 선택 문항용)", example = "3")
        private Integer answerNumber;

        @Schema(description = "다중 선택 답변 번호 목록 (다중 선택 문항용)", example = "[1, 2, 3]")
        private List<Integer> answerNumbers;

        public boolean isMultipleChoice() {
            return answerNumbers != null && !answerNumbers.isEmpty();
        }

        public boolean isSingleChoice() {
            return answerNumber != null;
        }
    }
} 