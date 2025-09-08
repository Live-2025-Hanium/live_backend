package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFeedbackForLLMDto {

    @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
    private String missionTitle;

    @Schema(description = "미션 카테고리", example = "HEALTH")
    private MissionCategory missionCategory;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDifficulty missionDifficulty;

    @Schema(description = "미션 타입", example = "TIMER")
    private CloverType  cloverType;

    @Schema(description = "체감 난이도", example = "EASY")
    private MissionDifficulty feedbackDifficulty;

    @Schema(description = "피드백 코멘트", example = "코멘트...")
    private String feedbackComment;

    public static UserFeedbackForLLMDto from(CloverMissionRecord missionRecord) {
        return UserFeedbackForLLMDto.builder()
                .missionTitle(missionRecord.getMissionTitle())
                .missionCategory(missionRecord.getMissionCategory())
                .missionDifficulty(missionRecord.getMissionDifficulty())
                .cloverType(missionRecord.getCloverType())
                .feedbackDifficulty(missionRecord.getFeedbackDifficulty())
                .feedbackComment(missionRecord.getFeedbackComment())
                .build();
    }
}
