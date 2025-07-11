package com.example.live_backend.domain.clover.service;

import com.example.live_backend.domain.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.clover.entity.MissionUser;
import com.example.live_backend.domain.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.memeber.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionService {

    private final CloverMissionRepository cloverMissionRepository;
    private final UserUtil userUtil;

    @Transactional
    public CloverMissionListResponseDto getCloverMissionList() {
        Long currentUserId = userUtil.getCurrentUserId();
        log.info("클로버 미션 리스트 조회 시작 - 인증된 사용자 ID: {}", currentUserId);

        LocalDateTime searchDate = LocalDateTime.now();

        List<MissionUser> todayMissions = cloverMissionRepository.findTodayCloverMissionsByUserId(currentUserId, searchDate);

        log.info("조회된 클로버 미션 개수: {}", todayMissions.size());

        // MissionUser 리스트를 TodayMissionDto 리스트로 변환
        List<CloverMissionListResponseDto.TodayMissionDto> missionDtos = todayMissions.stream()
                .map(this::convertToTodayMissionDto)
                .collect(Collectors.toList());

        CloverMissionListResponseDto response = CloverMissionListResponseDto.builder()
                .userId(currentUserId)
                .searchDate(searchDate)
                .missions(missionDtos)
                .build();

        log.info("클로버 미션 리스트 조회 완료 - 총 {}개 미션", missionDtos.size());

        return response;
    }

    /**
     * MissionUser -> TodayMissionDto 변환
     */
    private CloverMissionListResponseDto.TodayMissionDto convertToTodayMissionDto(MissionUser missionUser) {
        return CloverMissionListResponseDto.TodayMissionDto.builder()
                .missionId(missionUser.getId())
                .title(missionUser.getTitle())
                .build();
    }

    @Transactional
    public CloverMissionResponseDto getCloverMission(Long missionId) {

        Long currentUserId = userUtil.getCurrentUserId();
        log.info("클로버 미션 조회 시작 - 인증된 사용자 ID: {}", currentUserId);

        MissionUser cloverMissionById = cloverMissionRepository.findCloverMissionById(missionId);

        CloverMissionResponseDto response = CloverMissionResponseDto.builder()
                .missionId(cloverMissionById.getId())
                .title(cloverMissionById.getTitle())
                .description(cloverMissionById.getDescription())
                .category(cloverMissionById.getMissionDefault().getCategory())
                .difficulty(cloverMissionById.getMissionDefault().getDifficulty())
                .build();

        log.info("클로버 미션 조회 완료 - 클로버 미션 ID: {}", missionId);

        return response;

    }
}
