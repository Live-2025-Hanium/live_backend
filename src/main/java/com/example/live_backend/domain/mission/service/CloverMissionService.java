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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloverMissionService {

    private final MissionRecordRepository missionRecordRepository;
    private final VectorDBService vectorDBService;
    private final CloverMissionRepository cloverMissionRepository;
    private final MemberRepository memberRepository;
    private final CloverMissionDtoConverter cloverMissionDtoConverter;

    @Transactional
    public CloverMissionListResponseDto getCloverMissionList(Long userId) {

        Optional<Member> member = memberRepository.findById(userId);

        if (member.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        LocalDateTime searchDate = LocalDateTime.now();

        List<MissionRecord> todayMissions = missionRecordRepository.findCloverMissions(userId, searchDate);

        // 만약 오늘의 클로버 미션 리스트를 조회했는데 결과가 없다면 미션 할당받는 아래의 로직 수행
        if (todayMissions.isEmpty()) {

            // 예외를 던지는게 아니라.. 가장 최근 설문 정보를 DB 에서 조회해와서 설문 내용 요약하고, 벡터DB 에서 유사성 검색해서 10개 뽑고 LLM 에 전달해서 최종 3개 뽑는 플로우
            // 설문의 내용을 일단은 가져올 수가 없으니까 설문 내용을 하드코딩해서 기능 구현하도록 함
            String tempSurveySummary = "집안에서 컴퓨터만 보고 있으니 너무 답답해요. 하늘이나 자연을 보면서 마음을 정화하고 싶고, 산책도 좋아요.";

            List<Long> missionIds = vectorDBService.searchSimilarMissionsIds(tempSurveySummary, 3);

            List<CloverMission> findMissions = cloverMissionRepository.findAllById(missionIds);

            List<MissionRecord> newMissionRecordList = findMissions.stream()
                    .map(cloverMission -> MissionRecord.fromCloverMission(cloverMission, member.get()))
                    .toList();

            List<MissionRecord> savedMissionRecordList = missionRecordRepository.saveAll(newMissionRecordList);

            return CloverMissionListResponseDto.of(userId, savedMissionRecordList);
        }

        return CloverMissionListResponseDto.of(userId, todayMissions);
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
}
