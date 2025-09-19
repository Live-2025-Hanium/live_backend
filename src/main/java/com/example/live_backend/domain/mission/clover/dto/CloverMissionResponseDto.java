package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
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
public class CloverMissionResponseDto {

    @Schema(description = "미션 기록 ID", example = "10")
    private Long userMissionId;

    @Schema(description = "미션 타입", example = "TIMER / DISTANCE / PHOTO / VISIT")
    private String cloverType;

    @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
    private String missionTitle;

    @Schema(description = "미션 수행 상태", example = "ASSIGNED / STARTED / PAUSED / COMPLETED")
    private CloverMissionStatus missionStatus;

    @Schema(description = "미션 카테고리", example = "EASY")
    private MissionCategory missionCategory;

    @Schema(description = "미션 난이도", example = "EASY")
    private MissionDifficulty missionDifficulty;

    @Schema(description = "타이머 미션 남은 시간", example = "10:30")
    private String remainingTime;

    @Schema(description = "거리 미션 남은 거리", example = "500")
    private Integer remainingDistance;

    @Schema(description = "추천 장소 이름", example = "스타벅스 OO점")
    private String placeName;

    @Schema(description = "추천 장소 주소", example = "서울시 강남구 ...")
    private String address;

    @Schema(description = "추천 장소 위도", example = "37.123456")
    private String latitude;

    @Schema(description = "추천 장소 경도", example = "127.123456")
    private String longitude;

    @Schema(description = "일러스트레이션 주소", example = "S3 URL")
    private String illustrationUrl;

    public static CloverMissionResponseDto from(CloverMissionRecord missionRecord) {

        String missionTitle = missionRecord.getMissionTitle();
        if (missionRecord.getCloverType() == CloverType.VISIT && missionRecord.getPlaceName() != null && !missionRecord.getPlaceName().isEmpty()) {
            missionTitle = String.format("%s (%s)", missionRecord.getMissionTitle(), missionRecord.getPlaceName());
        }

        CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder = CloverMissionResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .cloverType(String.valueOf(missionRecord.getCloverType()))
                .missionTitle(missionTitle)
                .missionStatus(missionRecord.getCloverMissionStatus())
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

    private static void addDistanceInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        int remainingDistance = missionRecord.getRequiredMeters() - missionRecord.getProgressInMeters();
        builder.remainingDistance(Math.max(0, remainingDistance));
    }

    private static void addTimerInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        int remainingSeconds = missionRecord.getRequiredSeconds() - missionRecord.getProgressInSeconds();
        remainingSeconds = Math.max(0, remainingSeconds);

        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        builder.remainingTime(formattedTime);
    }

    private static void addVisitInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        builder.placeName(missionRecord.getPlaceName())
                .address(missionRecord.getAddress())
                .latitude(missionRecord.getLatitude())
                .longitude(missionRecord.getLongitude());
    }

    private static void addPhotoInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, CloverMissionRecord missionRecord) {
        builder.illustrationUrl(missionRecord.getIllustrationUrl());
    }
}
