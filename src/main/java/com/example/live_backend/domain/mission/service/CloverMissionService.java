package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.mission.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.memeber.util.UserUtil;
import com.example.live_backend.domain.mission.repository.MissionRecordRepository;
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
            throw new RuntimeException("조회된 클로버 미션 개수: 0");
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
    public CloverMissionResponseDto getCloverMissionInfo(Long missionRecordId) {

        Long currentUserId = userUtil.getCurrentUserId();
        log.info("클로버 미션 조회 시작 - 인증된 사용자 ID: {}", currentUserId);

        MissionRecord cloverMissionById = missionRecordRepository.findById(missionRecordId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 미션을 찾을 수 없습니다: " + missionRecordId));

        CloverMissionResponseDto response = CloverMissionResponseDto.builder()
                .missionRecordId(cloverMissionById.getId())
                .title(cloverMissionById.getMissionTitle())
                .description(cloverMissionById.getMissionDescription())
                .category(cloverMissionById.getMissionCategory())
                .difficulty(cloverMissionById.getMissionDifficulty())
                .build();

        return response;
    }
}
