package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloverMissionRecordResponseDto {

    @Schema(description = "미션 기록 ID", example = "10")
    private Long userMissionId;

    @Schema(description = "미션 타입", example = "TIMER / DISTANCE / PHOTO / VISIT")
    private String cloverType;

    @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
    private String missionTitle;

    @Schema(description = "미션 수행 상태", example = "ASSIGNED / STARTED / PAUSED / COMPLETED")
    private CloverMissionStatus missionStatus;

    @Schema(description = "완료 시간", example = "2023-12-25T10:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "미션 카테고리", example = "EASY")
    private MissionCategory missionCategory;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDifficulty missionDifficulty;

    @Schema(description = "피드백 코멘트", example = "미션을 완료하면서 정말 뿌듯했습니다!")
    private String feedbackComment;

    @Schema(description = "체감 난이도", example = "EASY")
    private MissionDifficulty feedbackDifficulty;

    @Schema(description = "인증샷 이미지 URL", example = "https://s3.amazonaws.com/bucket/mission-certification/image.jpg")
    private String imageUrl;

    public static CloverMissionRecordResponseDto from(CloverMissionRecord missionRecord) {
        return CloverMissionRecordResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionTitle(missionRecord.getMissionTitle())
                .cloverType(String.valueOf(missionRecord.getCloverType()))
                .missionStatus(missionRecord.getCloverMissionStatus())
                .completedAt(missionRecord.getCompletedAt())
                .missionCategory(missionRecord.getMissionCategory())
                .missionDifficulty(missionRecord.getMissionDifficulty())
                .feedbackComment(missionRecord.getFeedbackComment())
                .feedbackDifficulty(missionRecord.getFeedbackDifficulty())
                .imageUrl(missionRecord.getImageUrl())
                .build();
    }
}
