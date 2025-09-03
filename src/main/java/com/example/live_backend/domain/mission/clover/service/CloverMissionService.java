package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.clover.dto.*;
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

    private final CloverMissionRecordService cloverMissionRecordService;
    private final LLMBasedQueryGeneratorService llmBasedQueryGeneratorService;

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

        String vectorSearchQuery;
        List<String> negativeKeywords = List.of(); // 추가 예정

        try {
            // 1. 사용자의 최근 미션 피드백 3개 조회
            List<UserFeedbackForLLMDto> recentFeedbacks =
                    cloverMissionRecordService.getRecentMissionRecordsWithFeedback(member.getId());

            if (!recentFeedbacks.isEmpty()) {
                // 2. LLM을 통해 사용자 상태 분석 및 벡터 검색 쿼리 생성
                LLMProcessingResultDto llmResult =
                        llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(recentFeedbacks);

                vectorSearchQuery = llmResult.getExpectedEffect();
                negativeKeywords = llmResult.getNegativeKeywords();
            } else {
                // 3. 최근 피드백이 없는 경우 기본 쿼리 사용
                vectorSearchQuery = generateDefaultSearchQuery();
            }

        } catch (Exception e) {
            // 4. LLM 처리 실패 시 fallback 처리
            vectorSearchQuery = generateDefaultSearchQuery();
        }

        List<Long> newMissionsIds = vectorDBService.searchSimilarMissionsIds(vectorSearchQuery, 3, excludedIds);

        List<CloverMission> findMissions = cloverMissionRepository.findAllById(newMissionsIds);

        // TODO: negative keywords 로 미션 필터링 기능 추가 예정

        List<CloverMissionRecord> newMissionRecordList = findMissions.stream()
                .map(cloverMission -> CloverMissionRecord.from(cloverMission, member))
                .toList();

        return cloverMissionRecordRepository.saveAll(newMissionRecordList);
    }

    private Member findUser(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateDefaultSearchQuery() {
        return "처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션";
    }
}
