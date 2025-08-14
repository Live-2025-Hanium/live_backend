package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private CloverMissionStatus cloverMissionStatus;

    @Schema(description = "미션 카테고리", example = "EASY")
    private MissionCategory missionCategory;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDifficulty missionDifficulty;

    @Schema(description = "타이머 미션 남은 시간", example = "10:30")
    private String remainingTime;

    @Schema(description = "거리 미션 남은 거리", example = "500")
    private Integer remainingDistance;

    @Schema(description = "방문 미션 주소", example = "주소정보")
    private String targetAddress;

    @Schema(description = "일러스트레이션 주소", example = "S3 URL")
    private String illustrationUrl;

    public static CloverMissionRecordResponseDto from(CloverMissionRecord missionRecord) {
        CloverMissionRecordResponseDto.CloverMissionRecordResponseDtoBuilder builder = CloverMissionRecordResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .cloverType(String.valueOf(missionRecord.getCloverType()))
                .missionTitle(missionRecord.getMissionTitle())
                .cloverMissionStatus(missionRecord.getCloverMissionStatus())
                .missionDifficulty(missionRecord.getMissionDifficulty())
                .missionCategory(missionRecord.getMissionCategory());

        switch (missionRecord.getCloverType()) {
            case DISTANCE:
                addDistanceInfo(builder, missionRecord);
                break;
            case TIMER:
                addTimerInfo(builder, missionRecord);
                break;
            case VISIT:
                addVisitInfo(builder, missionRecord);
                break;
            case PHOTO:
                addPhotoInfo(builder, missionRecord);
                break;
            default:
                throw new CustomException(ErrorCode.UNSUPPORTED_CLOVER_TYPE);
        }

        return builder.build();
    }

    private static void addDistanceInfo(CloverMissionRecordResponseDto.CloverMissionRecordResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        int remainingDistance = missionRecord.getRequiredMeters() - missionRecord.getProgressInMeters();
        builder.remainingDistance(Math.max(0, remainingDistance));
    }

    private static void addTimerInfo(CloverMissionRecordResponseDto.CloverMissionRecordResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        int remainingSeconds = missionRecord.getRequiredSeconds() - missionRecord.getProgressInSeconds();
        remainingSeconds = Math.max(0, remainingSeconds);

        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        builder.remainingTime(formattedTime);
    }

    private static void addVisitInfo(CloverMissionRecordResponseDto.CloverMissionRecordResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        builder.targetAddress(missionRecord.getTargetAddress());
    }

    private static void addPhotoInfo(CloverMissionRecordResponseDto.CloverMissionRecordResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        builder.illustrationUrl(missionRecord.getIllustrationUrl());
    }
}
