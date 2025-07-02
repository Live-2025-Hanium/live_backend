package com.example.live_backend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponseListDto {

    @Schema(description = "설문 응답 ID", example = "123")
    private Long responseId;

    @Schema(description = "사용자 ID", example = "456")
    private Long userId;

    @Schema(description = "응답 완료 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "설문 답변 목록")
    private List<SurveyAnswerDetailDto> answers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveyAnswerDetailDto {
        
        @Schema(description = "문제 번호", example = "1")
        private Integer questionNumber;

        @Schema(description = "선택한 답변 번호", example = "3")
        private Integer answerNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSurveyResponseListDto {
        
        @Schema(description = "사용자 ID", example = "456")
        private Long userId;

        @Schema(description = "총 응답 횟수", example = "3")
        private Long totalResponseCount;

        @Schema(description = "설문 응답 목록")
        private List<SurveyResponseListDto> responses;
    }
} 