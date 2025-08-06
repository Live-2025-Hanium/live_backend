package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.mission.Enum.CloverType;
import com.example.live_backend.domain.mission.dto.MissionRecordRequestDto;
import com.example.live_backend.domain.mission.dto.MissionRecordResponseDto;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.mission.repository.MissionRecordRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionRecordService {

    private final MissionRecordRepository missionRecordRepository;

    @Transactional
    public MissionRecordResponseDto addMissionRecord(Long memberId, MissionRecordRequestDto requestDto) {
        MissionRecord missionRecord = missionRecordRepository.findByIdWithMember(requestDto.getUserMissionId())
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!missionRecord.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        if (missionRecord.getCloverType() == CloverType.PHOTO) {
            if (requestDto.getImageUrl() == null || requestDto.getImageUrl().trim().isEmpty()) {
                throw new CustomException(ErrorCode.IMAGE_URL_REQUIRED);
            }
            missionRecord.addFeedbackWithImage(
                    requestDto.getFeedbackComment(), 
                    requestDto.getFeedbackDifficulty(),
                    requestDto.getImageUrl()
            );
        } else {
            missionRecord.addFeedback(
                    requestDto.getFeedbackComment(),
                    requestDto.getFeedbackDifficulty());
        }

        return MissionRecordResponseDto.from(missionRecord);
    }

    @Transactional(readOnly = true)
    public MissionRecordResponseDto getMissionRecord(Long memberId, Long userMissionId) {
        MissionRecord missionRecord = missionRecordRepository.findByIdWithMember(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!missionRecord.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        return MissionRecordResponseDto.from(missionRecord);
    }

    @Transactional
    public MissionRecordResponseDto updateMissionRecord(Long memberId, MissionRecordRequestDto requestDto) {
        MissionRecord missionRecord = missionRecordRepository.findByIdWithMember(requestDto.getUserMissionId())
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!missionRecord.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        if (missionRecord.getCloverType() == CloverType.PHOTO && requestDto.getImageUrl() != null) {
            if (requestDto.getImageUrl().trim().isEmpty()) {
                throw new CustomException(ErrorCode.IMAGE_URL_REQUIRED);
            }

            missionRecord.updateFeedbackWithImage(
                    requestDto.getFeedbackComment(),
                    requestDto.getFeedbackDifficulty(),
                    requestDto.getImageUrl()
            );
        } else {
            missionRecord.updateFeedback(
                    requestDto.getFeedbackComment(),
                    requestDto.getFeedbackDifficulty());
        }

        return MissionRecordResponseDto.from(missionRecord);
    }
}