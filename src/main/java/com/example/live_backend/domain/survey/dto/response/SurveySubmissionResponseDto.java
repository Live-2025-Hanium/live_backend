package com.example.live_backend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveySubmissionResponseDto {

    @Schema(description = "설문 응답 ID", example = "123")
    private Long responseId;

    @Schema(description = "응답 완료 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "응답한 문제 수", example = "5")
    private Integer totalAnswers;
} 