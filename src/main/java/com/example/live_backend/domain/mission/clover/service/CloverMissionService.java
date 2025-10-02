package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.clover.entity.CloverHistory;
import com.example.live_backend.domain.clover.repository.CloverHistoryRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.clover.Enum.MissionScoreCalculator;
import com.example.live_backend.domain.mission.clover.dto.*;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.entity.VisitMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionVectorRepository;
import com.example.live_backend.domain.survey.vitality.enums.VitalityLevel;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.infra.kakao.feign.KakaoLocalFeign;
import com.example.live_backend.infra.kakao.feign.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.example.live_backend.global.constant.CloverConstants.CLOVER_MISSION_REWARD;
import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloverMissionService {

    private final CloverMissionVectorRepository cloverMissionVectorRepository;
    private final CloverMissionRepository cloverMissionRepository;
    private final MemberRepository memberRepository;

    private final CloverMissionRecordRepository cloverMissionRecordRepository;
    private final KakaoLocalFeign kakaoLocalFeign;

    private final CloverMissionRecordService cloverMissionRecordService;
    private final LLMBasedQueryGeneratorService llmBasedQueryGeneratorService;
    private final CloverHistoryRepository cloverHistoryRepository;

    private static final int DEFAULT_MISSION_COUNT = 10;
    private static final int KAKAO_SEARCH_RADIUS = 2000;
    private static final int KAKAO_SEARCH_MAX_RADIUS = 20000;
    private static final int KAKAO_SEARCH_SIZE = 5;
    private static final int REQUIRED_MISSION_COUNT = 3;

    @Transactional
    public CloverMissionListResponseDto getCloverMissionList(Long memberId, BigDecimal lat, BigDecimal lon) {

        Member member = findUser(memberId);
        LocalDate today  = LocalDate.now();
        List<CloverMissionRecord> todayMissions = cloverMissionRecordRepository.findCloverMissionsList(memberId, today);

        // 만약 오늘의 클로버 미션 리스트를 조회했는데 결과가 없다면 미션 할당받는 아래의 로직 수행
        if (todayMissions.isEmpty()) {
            List<CloverMissionRecord> newMissions = assignNewCloverMissions(member, emptyList(), lat, lon);
            return CloverMissionListResponseDto.of(memberId, newMissions);
        }

        return CloverMissionListResponseDto.of(memberId, todayMissions);
    }

    @Transactional
    public CloverMissionListResponseDto assignCloverMissionList(Long memberId, BigDecimal lat, BigDecimal lon) {

        Member member = findUser(memberId);
        LocalDate today  = LocalDate.now();

        List<CloverMissionRecord> todayAllMissions = cloverMissionRecordRepository.findCloverMissionsList(memberId, today);

        List<Long> excludedMissionIds = todayAllMissions.stream()
                .map(CloverMissionRecord::getMissionId)
                .toList();

        List<CloverMissionRecord> newMissions = assignNewCloverMissions(member, excludedMissionIds, lat, lon);

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

        Member member = missionRecord.getMember();
        member.increaseCloverCount(CLOVER_MISSION_REWARD);

        CloverHistory cloverHistory = CloverHistory.fromMissionCompletion(member, missionRecord.getMissionTitle());
        cloverHistoryRepository.save(cloverHistory);

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

    private List<CloverMissionRecord> assignNewCloverMissions(Member member, List<Long> excludedIds, BigDecimal lat, BigDecimal lon) {

        LLMProcessingResultDto strategy = generateSearchStrategy(member.getId());

        List<CloverMission> missions = findSimilarMissions(strategy.getSearchQuery(), excludedIds);

        // 벡터 DB에서 가져온 미션들에 필터링 적용
        List<CloverMission> weightedMissions = applyMissionWeighting(missions, strategy);

        return selectAndSaveFinalMissions(weightedMissions, member, lat, lon);
    }

    private Member findUser(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private List<CloverMissionRecord> selectAndSaveFinalMissions(
            List<CloverMission> weightedMissions, Member member, BigDecimal lat, BigDecimal lon) {

        List<CloverMissionRecord> finalMissionRecords = new ArrayList<>();

        for (CloverMission mission : weightedMissions) {
            if (finalMissionRecords.size() >= REQUIRED_MISSION_COUNT) {
                break;
            }

            buildCompletableMissionRecord(mission, member, lat, lon).ifPresent(finalMissionRecords::add);
        }

        if (finalMissionRecords.size() < REQUIRED_MISSION_COUNT) {
            log.warn("미션 {} 개를 할당할 수 없습니다. 사용자({})에게 미션 {} 개만 할당되었습니다.",
                    REQUIRED_MISSION_COUNT, member.getId(), finalMissionRecords.size());
        }

        return cloverMissionRecordRepository.saveAll(finalMissionRecords);
    }

    private Optional<CloverMissionRecord> buildCompletableMissionRecord(CloverMission mission, Member member, BigDecimal lat, BigDecimal lon) {
        CloverMissionRecord record = CloverMissionRecord.from(mission, member);

        if (mission instanceof VisitMission visitMission) {
            return findPlaceForVisitMission(visitMission, lat, lon)
                    .map(place -> {
                        record.setVisitPlace(place.getPlaceName(), place.getAddressName(), place.getY(), place.getX());
                        return record;
                    });
        }
        return Optional.of(record);
    }

    private Optional<KakaoKeywordResponse.KakaoPlace> findPlaceForVisitMission(VisitMission mission, BigDecimal lat, BigDecimal lon) {
        KakaoKeywordResponse response = kakaoLocalFeign.searchByKeyword(
                mission.getTargetPlaceCategory(), lon.doubleValue(), lat.doubleValue(),
                KAKAO_SEARCH_RADIUS, 1, KAKAO_SEARCH_SIZE, "distance"
        );

        if (response.getDocuments().isEmpty()) {
            response = kakaoLocalFeign.searchByKeyword(
                    mission.getTargetPlaceCategory(), lon.doubleValue(), lat.doubleValue(),
                    KAKAO_SEARCH_MAX_RADIUS, 1, KAKAO_SEARCH_SIZE, "distance"
            );
        }

        if (response.getDocuments().isEmpty()) {
            return Optional.empty();
        }

        List<KakaoKeywordResponse.KakaoPlace> places = response.getDocuments();
        return Optional.of(places.get(new Random().nextInt(places.size())));
    }

    private LLMProcessingResultDto generateSearchStrategy(Long memberId) {
        try {

            Member member = findUser(memberId);
            VitalityLevel vitalityLevel = member.getVitalityLevel();

            List<UserFeedbackForLLMDto> userFeedbackList =
                    cloverMissionRecordService.getRecentMissionRecordsWithFeedback(memberId);

            if (userFeedbackList.isEmpty()) {
                return generateDefaultSearchQuery();
            }

            LLMProcessingResultDto llmResult =
                    llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(userFeedbackList, vitalityLevel);

            return llmResult;

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

        return cloverMissionRepository.findAllById(missionIds);
    }

    private LLMProcessingResultDto generateDefaultSearchQuery() {
        return LLMProcessingResultDto.builder()
                .searchQuery("처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션")
                .build();
    }

    private List<CloverMission> applyMissionWeighting(List<CloverMission> missions, LLMProcessingResultDto strategy) {
        if (strategy == null) {
            return missions;
        }

        return missions.stream()
                .sorted(Comparator.comparingInt(m -> calculateMissionScore((CloverMission) m, strategy)).reversed())
                .toList();
    }

    private int calculateMissionScore(CloverMission mission, LLMProcessingResultDto strategy) {
        return MissionScoreCalculator.CATEGORY.calculate(mission, strategy) +
                MissionScoreCalculator.DIFFICULTY.calculate(mission, strategy) +
                MissionScoreCalculator.CLOVER_TYPE.calculate(mission, strategy);
    }
}
