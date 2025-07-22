package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.mission.Enum.MissionStatus;
import com.example.live_backend.domain.mission.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.memeber.util.UserUtil;
import com.example.live_backend.domain.mission.repository.MissionRecordRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloverMissionService {

    private final MissionRecordRepository missionRecordRepository;
    private final UserUtil userUtil;

    @Transactional(readOnly = true)
    public CloverMissionListResponseDto getCloverMissionList() {
        Long currentUserId = userUtil.getCurrentUserId();
        log.info("클로버 미션 리스트 조회 시작 - 인증된 사용자 ID: {}", currentUserId);

        LocalDateTime searchDate = LocalDateTime.now();

        List<MissionRecord> todayMissions = missionRecordRepository.findCloverMissions(currentUserId, searchDate);

        if (todayMissions.isEmpty()) {
            log.info("조회된 클로버 미션 개수: 0");
            throw new CustomException(ErrorCode.MISSION_NOT_FOUND);
        }

        log.info("조회된 클로버 미션 개수: {}", todayMissions.size());

        // MissionRecord 리스트를 TodayMissionDto 리스트로 변환
        List<CloverMissionListResponseDto.CloverMissionList> missionLists = todayMissions.stream()
                .map(missionRecord -> CloverMissionListResponseDto.CloverMissionList.builder()
                        .missionRecordId(missionRecord.getId())
                        .title(missionRecord.getMissionTitle())
                        .build())
                .toList();

        CloverMissionListResponseDto response = CloverMissionListResponseDto.builder()
                .userId(currentUserId)
                .missions(missionLists)
                .build();

        log.info("클로버 미션 리스트 조회 완료 - 총 {}개 미션", missionLists.size());

        return response;
    }

    @Transactional(readOnly = true)
    public CloverMissionResponseDto getCloverMissionInfo(Long userMissionId) {

        Long currentUserId = userUtil.getCurrentUserId();
        log.info("클로버 미션 조회 시작 - 인증된 사용자 ID: {}", currentUserId);

        MissionRecord cloverMissionById = missionRecordRepository.findById(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        CloverMissionResponseDto response = CloverMissionResponseDto.builder()
                .missionRecordId(cloverMissionById.getId())
                .title(cloverMissionById.getMissionTitle())
                .description(cloverMissionById.getMissionDescription())
                .category(cloverMissionById.getMissionCategory())
                .difficulty(cloverMissionById.getMissionDifficulty())
                .build();

        return response;
    }

    /**
     * 미션 상태 변경 - Started (ASSIGNED Or PAUSED -> STARTED)
     */
    @Transactional
    public CloverMissionStatusResponseDto startCloverMission(Long userMissionId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId);

        missionRecord.startMission();
        log.info("미션 상태 변경 완료: {}", missionRecord.getMissionStatus());

        CloverMissionStatusResponseDto response = CloverMissionStatusResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionTitle(missionRecord.getMissionTitle())
                .missionStatus(missionRecord.getMissionStatus())
                .assignedDate(missionRecord.getAssignedDate())
                .build();

        return response;
    }

    /**
     * 미션 상태 변경 - Paused (STARTED -> PAUSED)
     */
    @Transactional
    public CloverMissionStatusResponseDto pauseCloverMission(Long userMissionId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId);

        missionRecord.pauseMission();
        log.info("미션 상태 변경 완료: {}", MissionStatus.PAUSED);

        CloverMissionStatusResponseDto response = CloverMissionStatusResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionTitle(missionRecord.getMissionTitle())
                .missionStatus(missionRecord.getMissionStatus())
                .assignedDate(missionRecord.getAssignedDate())
                .build();

        return response;
    }

    /**
     * 미션 상태 변경 - Completed (STARTED -> Completed)
     */
    @Transactional
    public CloverMissionStatusResponseDto completeCloverMission(Long userMissionId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId);

        missionRecord.completeMission();
        log.info("미션 상태 변경 완료: {}", MissionStatus.COMPLETED);

        // DTO 생성
        CloverMissionStatusResponseDto response = CloverMissionStatusResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionTitle(missionRecord.getMissionTitle())
                .missionStatus(missionRecord.getMissionStatus())
                .assignedDate(missionRecord.getAssignedDate())
                .build();

        return response;
    }

    private MissionRecord findAndVerifyMissionRecord(Long userMissionId) {
        Long currentUserId = userUtil.getCurrentUserId();
        log.info("미션 조회  및 검증시작 - 사용자 ID: {}, 미션 ID: {}", currentUserId, userMissionId);

        MissionRecord findByUserMissionId = missionRecordRepository.findByIdWithUser(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!findByUserMissionId.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        return findByUserMissionId;
    }
}
