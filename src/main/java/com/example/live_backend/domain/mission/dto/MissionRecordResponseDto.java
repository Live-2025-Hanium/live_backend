package com.example.live_backend.domain.mission.dto;

import com.example.live_backend.domain.mission.Enum.*;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "미션 기록 응답 DTO")
public class MissionRecordResponseDto {

    @Schema(description = "미션 기록 ID", example = "1")
    private Long userMissionId;

    @Schema(description = "미션 제목", example = "5km 걷기")
    private String missionTitle;

    @Schema(description = "클로버 미션 타입", example = "PHOTO / TIMER / DISTANCE / VISIT")
    private CloverType cloverType;

    @Schema(description = "미션 상태", example = "COMPLETED")
    private CloverMissionStatus cloverMissionStatus;

    @Schema(description = "완료 시간", example = "2023-12-25T10:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "미션 카테고리", example = "COMMUNICATION")
    private MissionCategory missionCategory;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDifficulty missionDifficulty;

    @Schema(description = "피드백 코멘트", example = "미션을 완료하면서 정말 뿌듯했습니다!")
    private String feedbackComment;

    @Schema(description = "체감 난이도", example = "EASY")
    private MissionDifficulty feedbackDifficulty;

    @Schema(description = "인증샷 이미지 URL", example = "https://s3.amazonaws.com/bucket/mission-certification/image.jpg")
    private String imageUrl;

    public static MissionRecordResponseDto from(MissionRecord missionRecord) {
        return MissionRecordResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionTitle(missionRecord.getMissionTitle())
                .cloverType(missionRecord.getCloverType())
                .cloverMissionStatus(missionRecord.getCloverMissionStatus())
                .completedAt(missionRecord.getCompletedAt())
                .missionCategory(missionRecord.getMissionCategory())
                .missionDifficulty(missionRecord.getMissionDifficulty())
                .feedbackComment(missionRecord.getFeedbackComment())
                .feedbackDifficulty(missionRecord.getFeedbackDifficulty())
                .imageUrl(missionRecord.getImageUrl())
                .build();
    }
}