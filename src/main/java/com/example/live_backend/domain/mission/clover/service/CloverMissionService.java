package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.clover.dto.*;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionVectorRepository;
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

    private final CloverMissionVectorRepository cloverMissionVectorRepository;
    private final CloverMissionRepository cloverMissionRepository;
    private final MemberRepository memberRepository;

    private final CloverMissionRecordRepository cloverMissionRecordRepository;

    private final CloverMissionRecordService cloverMissionRecordService;
    private final LLMBasedQueryGeneratorService llmBasedQueryGeneratorService;

    private static final int DEFAULT_MISSION_COUNT = 3;

    @Transactional
    public CloverMissionListResponseDto getCloverMissionList(Long memberId) {

        Member member = findUser(memberId);
        LocalDate today  = LocalDate.now();
        List<CloverMissionRecord> todayMissions = cloverMissionRecordRepository.findCloverMissionsList(memberId, today);

        // 만약 오늘의 클로버 미션 리스트를 조회했는데 결과가 없다면 미션 할당받는 아래의 로직 수행
        if (todayMissions.isEmpty()) {
            List<CloverMissionRecord> newMissions = assignNewCloverMissions(member, emptyList());
            return CloverMissionListResponseDto.of(memberId, newMissions);
        }

        return CloverMissionListResponseDto.of(memberId, todayMissions);
    }

    @Transactional
    public CloverMissionListResponseDto assignCloverMissionList(Long memberId) {

        Member member = findUser(memberId);
        LocalDate today  = LocalDate.now();

        List<CloverMissionRecord> todayAllMissions = cloverMissionRecordRepository.findCloverMissionsList(memberId, today);

        List<Long> excludedMissionIds = todayAllMissions.stream()
                .map(CloverMissionRecord::getMissionId)
                .toList();

        List<CloverMissionRecord> newMissions = assignNewCloverMissions(member, excludedMissionIds);

        return CloverMissionListResponseDto.of(memberId, newMissions);
    }

    @Transactional(readOnly = true)
    public CloverMissionResponseDto getCloverMissionInfo(Long userMissionId, Long memberId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, memberId);

        return CloverMissionResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionStatusResponseDto startCloverMission(Long userMissionId, Long memberId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, memberId);

        missionRecord.startMission();

        return CloverMissionStatusResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionStatusResponseDto pauseCloverMission(Long userMissionId, Long memberId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId,memberId);

        missionRecord.pauseMission();

        return CloverMissionStatusResponseDto.from(missionRecord);
    }

    @Transactional
    public CloverMissionStatusResponseDto completeCloverMission(Long userMissionId, Long memberId) {

        CloverMissionRecord missionRecord = findAndVerifyMissionRecord(userMissionId, memberId);

        missionRecord.completeMission();

        return CloverMissionStatusResponseDto.from(missionRecord);
    }

    private CloverMissionRecord findAndVerifyMissionRecord(Long userMissionId, Long memberId) {

        CloverMissionRecord findByUserMissionId = cloverMissionRecordRepository.findByIdWithMember(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!findByUserMissionId.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        return findByUserMissionId;
    }

    private List<CloverMissionRecord> assignNewCloverMissions(Member member, List<Long> excludedIds) {

        String searchQuery = generateSearchQuery(member.getId());

        List<CloverMission> missions = findSimilarMissions(searchQuery, excludedIds);

        return createAndSaveMissionRecords(missions, member);
    }

    private Member findUser(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateSearchQuery(Long memberId) {
        try {
            List<UserFeedbackForLLMDto> userFeedbackList =
                    cloverMissionRecordService.getRecentMissionRecordsWithFeedback(memberId);

            if (userFeedbackList.isEmpty()) {
                return generateDefaultSearchQuery();
            }

            LLMProcessingResultDto llmResult =
                    llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(userFeedbackList);

            return llmResult.getExpectedEffect();

        } catch (Exception e) {
            log.warn("LLM 처리 실패, 기본 쿼리 사용: {}", e.getMessage());
            return generateDefaultSearchQuery();
        }
    }

    private List<CloverMission> findSimilarMissions(String searchQuery, List<Long> excludedIds) {
        List<Long> missionIds = cloverMissionVectorRepository.searchSimilarMissionsIds(
                searchQuery,
                DEFAULT_MISSION_COUNT,
                excludedIds
        );

        // TODO: negative keywords 로 미션 필터링 기능 추가 예정

        return cloverMissionRepository.findAllById(missionIds);
    }

    private List<CloverMissionRecord> createAndSaveMissionRecords(List<CloverMission> missions, Member member) {
        List<CloverMissionRecord> missionRecords = missions.stream()
                .map(mission -> CloverMissionRecord.from(mission, member))
                .toList();

        return cloverMissionRecordRepository.saveAll(missionRecords);
    }

    private String generateDefaultSearchQuery() {
        return "처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션";
    }
}
