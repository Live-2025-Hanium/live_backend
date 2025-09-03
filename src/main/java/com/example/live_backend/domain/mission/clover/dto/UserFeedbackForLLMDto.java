package com.example.live_backend.domain.mission.clover.dto;

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

    @Schema(description = "체감 난이도", example = "EASY")
    private MissionDifficulty feedbackDifficulty;

    @Schema(description = "피드백 코멘트", example = "미션을 완료하면서 정말 뿌듯했습니다!")
    private String feedbackComment;

    public static UserFeedbackForLLMDto from(CloverMissionRecord missionRecord) {
        return UserFeedbackForLLMDto.builder()
                .missionTitle(missionRecord.getMissionTitle())
                .feedbackDifficulty(missionRecord.getFeedbackDifficulty())
                .feedbackComment(missionRecord.getFeedbackComment())
                .build();
    }
}
