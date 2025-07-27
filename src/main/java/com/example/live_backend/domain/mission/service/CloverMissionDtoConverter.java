package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.mission.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CloverMissionDtoConverter {

    /**
     * MissionRecord 엔티티를 받아서, 그 타입에 맞는 CloverMissionResponseDto로 변환합니다.
     * @param missionRecord 변환할 엔티티
     * @return 변환된 CloverMissionResponseDto
     */
    public CloverMissionResponseDto convert(MissionRecord missionRecord) {
        CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder = CloverMissionResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .cloverType(String.valueOf(missionRecord.getCloverType()))
                .missionTitle(missionRecord.getMissionTitle())
                .description(missionRecord.getMissionDescription())
                .missionStatus(missionRecord.getMissionStatus())
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

    private void addDistanceInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, MissionRecord missionRecord) {
        int remainingDistance = missionRecord.getRequiredMeters() - missionRecord.getProgressInMeters();
        builder.remainingDistance(Math.max(0, remainingDistance));
    }

    private void addTimerInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, MissionRecord missionRecord) {
        int remainingSeconds = missionRecord.getRequiredSeconds() - missionRecord.getProgressInSeconds();
        remainingSeconds = Math.max(0, remainingSeconds);

        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        builder.remainingTime(formattedTime);
    }

    private void addVisitInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, MissionRecord missionRecord) {
        builder.targetAddress(missionRecord.getTargetAddress());
    }

    private void addPhotoInfo(CloverMissionResponseDto.CloverMissionResponseDtoBuilder builder, MissionRecord missionRecord) {
        builder.illustrationUrl(missionRecord.getIllustrationUrl());
    }
}
