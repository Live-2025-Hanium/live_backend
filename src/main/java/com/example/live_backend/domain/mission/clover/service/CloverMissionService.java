package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloverMissionService {

    private final VectorDBService vectorDBService;
    private final CloverMissionRepository cloverMissionRepository;
    private final MemberRepository memberRepository;

    private final CloverMissionRecordRepository cloverMissionRecordRepository;

    @Transactional
    public CloverMissionListResponseDto getCloverMissionList(Long userId) {

        Member member = findUser(userId);
        LocalDate today  = LocalDate.now();
        List<CloverMissionRecord> todayMissions = cloverMissionRecordRepository.findCloverMissionsList(userId, today);

        // 만약 오늘의 클로버 미션 리스트를 조회했는데 결과가 없다면 미션 할당받는 아래의 로직 수행
        if (todayMissions.isEmpty()) {
            List<CloverMissionRecord> newMissions = assignNewCloverMissions(member, emptyList());
            return CloverMissionListResponseDto.of(userId, newMissions);
        }

        return CloverMissionListResponseDto.of(userId, todayMissions);
    }

    @Transactional
    public CloverMissionListResponseDto assignCloverMissionList(Long userId) {

        Member member = findUser(userId);
        LocalDate today  = LocalDate.now();

        List<CloverMissionRecord> todayAllMissions = cloverMissionRecordRepository.findCloverMissionsList(userId, today);

        List<Long> excludedMissionIds = todayAllMissions.stream()
                .map(CloverMissionRecord::getMissionId)
                .toList();

        List<CloverMissionRecord> newMissions = assignNewCloverMissions(member, excludedMissionIds);

        return CloverMissionListResponseDto.of(userId, newMissions);
    }

    @Transactional(readOnly = true)
    public CloverMissionRecordResponseDto getCloverMissionInfo(Long userMissionId, Long userId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, userId);

        return CloverMissionRecordResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionStatusResponseDto startCloverMission(Long userMissionId, Long userId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, userId);

        missionRecord.startMission();

        return CloverMissionStatusResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionStatusResponseDto pauseCloverMission(Long userMissionId, Long userId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId,userId);

        missionRecord.pauseMission();

        return CloverMissionStatusResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionStatusResponseDto completeCloverMission(Long userMissionId, Long userId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, userId);

        missionRecord.completeMission();

        return CloverMissionStatusResponseDto.from(missionRecord);
    }

    private CloverMissionRecord findAndVerifyMissionRecord(Long userMissionId, Long userId) {

        CloverMissionRecord findByUserMissionId = cloverMissionRecordRepository.findByIdWithMember(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!findByUserMissionId.getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        return findByUserMissionId;
    }

    private List<CloverMissionRecord> assignNewCloverMissions(Member member, List<Long> excludedIds) {

        // TODO: 향후 실제 설문 요약 로직으로 대체 필요
        String tempSurveySummary = "집안에서 컴퓨터만 보고 있으니 너무 답답해요. 하늘이나 자연을 보면서 마음을 정화하고 싶고, 산책도 좋아요.";

        List<Long> newMissionsIds = vectorDBService.searchSimilarMissionsIds(tempSurveySummary, 3, excludedIds);

        List<CloverMission> findMissions = cloverMissionRepository.findAllById(newMissionsIds);
        List<CloverMissionRecord> newMissionRecordList = findMissions.stream()
                .map(cloverMission -> CloverMissionRecord.from(cloverMission, member))
                .toList();

        return cloverMissionRecordRepository.saveAll(newMissionRecordList);
    }

    private Member findUser(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
