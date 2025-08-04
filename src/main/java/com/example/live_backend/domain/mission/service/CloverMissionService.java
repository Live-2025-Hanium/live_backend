package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.entity.CloverMission;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.mission.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.repository.MissionRecordRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloverMissionService {

    private final MissionRecordRepository missionRecordRepository;
    private final VectorDBService vectorDBService;
    private final CloverMissionRepository cloverMissionRepository;
    private final MemberRepository memberRepository;
    private final CloverMissionDtoConverter cloverMissionDtoConverter;

    /**
     * 사용자의 클로버 미션 목록을 조회합니다.
     * 만약 오늘 날짜로 할당된 미션이 없다면, 새로운 미션을 자동으로 할당합니다.
     */
    @Transactional
    public CloverMissionListResponseDto getCloverMissionList(Long userId) {

        Member member = findUser(userId);
        LocalDateTime searchDate = LocalDateTime.now();
        List<MissionRecord> todayMissions = missionRecordRepository.findCloverMissions(userId, searchDate);

        // 만약 오늘의 클로버 미션 리스트를 조회했는데 결과가 없다면 미션 할당받는 아래의 로직 수행
        if (todayMissions.isEmpty()) {
            List<MissionRecord> newMissions = assignNewCloverMissions(member, emptyList());
            return CloverMissionListResponseDto.of(userId, newMissions);
        }

        return CloverMissionListResponseDto.of(userId, todayMissions);
    }

    /**
     * 사용자에게 새로운 클로버 미션 목록을 할당합니다.
     */
    @Transactional
    public CloverMissionListResponseDto assignCloverMissionList(Long userId) {
        Member member = findUser(userId);

        List<MissionRecord> todayAllMissions = missionRecordRepository.findCloverMissions(userId, LocalDateTime.now());

        List<Long> excludedMissionIds = todayAllMissions.stream()
                .map(record -> record.getMissionId())
                .toList();

        List<MissionRecord> newMissions = assignNewCloverMissions(member, excludedMissionIds);

        return CloverMissionListResponseDto.of(userId, newMissions);
    }

    @Transactional(readOnly = true)
    public CloverMissionResponseDto getCloverMissionInfo(Long userMissionId, Long userId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, userId);

        return cloverMissionDtoConverter.convert(missionRecord);
    }

    /**
     * 미션 상태 변경 - Started (ASSIGNED Or PAUSED -> STARTED)
     */
    @Transactional
    public CloverMissionStatusResponseDto startCloverMission(Long userMissionId, Long userId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, userId);

        missionRecord.startMission();

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
    public CloverMissionStatusResponseDto pauseCloverMission(Long userMissionId, Long userId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId,userId);

        missionRecord.pauseMission();

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
    public CloverMissionStatusResponseDto completeCloverMission(Long userMissionId, Long userId) {

        MissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, userId);

        missionRecord.completeMission();

        CloverMissionStatusResponseDto response = CloverMissionStatusResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionTitle(missionRecord.getMissionTitle())
                .missionStatus(missionRecord.getMissionStatus())
                .assignedDate(missionRecord.getAssignedDate())
                .build();

        return response;
    }

    private MissionRecord findAndVerifyMissionRecord(Long userMissionId, Long userId) {

        MissionRecord findByUserMissionId = missionRecordRepository.findByIdWithUser(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!findByUserMissionId.getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        return findByUserMissionId;
    }

    private List<MissionRecord> assignNewCloverMissions(Member member, List<Long> excludedIds) {

        // TODO: 향후 실제 설문 요약 로직으로 대체 필요
        String tempSurveySummary = "집안에서 컴퓨터만 보고 있으니 너무 답답해요. 하늘이나 자연을 보면서 마음을 정화하고 싶고, 산책도 좋아요.";

        List<Long> newMissionsIds = vectorDBService.searchSimilarMissionsIds(tempSurveySummary, 3, excludedIds);

        List<CloverMission> findMissions = cloverMissionRepository.findAllById(newMissionsIds);
        List<MissionRecord> newMissionRecordList = findMissions.stream()
                .map(cloverMission -> MissionRecord.fromCloverMission(cloverMission, member))
                .toList();

        return missionRecordRepository.saveAll(newMissionRecordList);
    }

    private Member findUser(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}
