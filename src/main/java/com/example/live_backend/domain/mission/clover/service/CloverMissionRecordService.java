package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordRequestDto;
import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloverMissionRecordService {

    private final CloverMissionRecordRepository missionRecordRepository;

    @Transactional
    public CloverMissionRecordResponseDto addMissionRecord(Long memberId, CloverMissionRecordRequestDto requestDto) {
        CloverMissionRecord missionRecord = missionRecordRepository.findByIdWithMember(requestDto.getUserMissionId())
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

        return CloverMissionRecordResponseDto.from(missionRecord);
    }

    @Transactional(readOnly = true)
    public com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto getMissionRecord(Long memberId, Long userMissionId) {
        CloverMissionRecord missionRecord = missionRecordRepository.findByIdWithMember(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!missionRecord.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        return CloverMissionRecordResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionRecordResponseDto updateMissionRecord(Long memberId, CloverMissionRecordRequestDto requestDto) {
        CloverMissionRecord missionRecord = missionRecordRepository.findByIdWithMember(requestDto.getUserMissionId())
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

        return CloverMissionRecordResponseDto.from(missionRecord);
    }
}