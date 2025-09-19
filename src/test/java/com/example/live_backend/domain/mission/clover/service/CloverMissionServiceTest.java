package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.dto.*;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.entity.DistanceMission;
import com.example.live_backend.domain.mission.clover.entity.TimerMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionVectorRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("클로버 미션 서비스 테스트")
class CloverMissionServiceTest {

    @InjectMocks
    private CloverMissionService cloverMissionService;

    @Mock
    private CloverMissionRecordRepository cloverMissionRecordRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CloverMissionVectorRepository cloverMissionVectorRepository;

    @Mock
    private CloverMissionRepository cloverMissionRepository;

    @Mock
    private CloverMissionRecordService cloverMissionRecordService;

    @Mock
    private LLMBasedQueryGeneratorService llmBasedQueryGeneratorService;

    private Member mockMember;
    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_USER_MISSION_ID = 10L;

    @BeforeEach
    void setUp() {

        mockMember = Member.builder()
                .email("mockuser@example.com")
                .oauthId("test-oauth-id")
                .role(Role.USER)
                .profile(new Profile("Mockuser", "https://example.com/profile.jpg"))
                .gender(Gender.FEMALE)
                .build();

        ReflectionTestUtils.setField(mockMember, "id", TEST_MEMBER_ID);
    }

    @Nested
    @DisplayName("클로버 미션 리스트 조회")
    class GetCloverMissionList {

        @Test
        @DisplayName("성공 - 오늘 생성된 클로버 미션이 이미 존재할 경우")
        void getCloverMissionList_Success_WhenMissionsExist() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));

            List<CloverMissionRecord> existingMissions = List.of(
                    createTestMissionRecord(101L, CloverMissionStatus.ASSIGNED, mockMember),
                    createTestMissionRecord(102L, CloverMissionStatus.STARTED, mockMember),
                    createTestMissionRecord(103L, CloverMissionStatus.PAUSED, mockMember)
            );
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(existingMissions);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_MEMBER_ID);
            assertThat(result.getMissions().size()).isEqualTo(3);

            verify(cloverMissionVectorRepository, never()).searchSimilarMissionsIds(anyString(), anyInt(), anyList());
            verify(cloverMissionRepository, never()).findAllById(any());
            verify(cloverMissionRecordRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("성공 - 오늘 생성된 미션이 없어 새로 할당받는 경우")
        void getCloverMissionList_Success_WhenMissionsAreNewlyAssigned() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(Collections.emptyList());

            List<Long> missionIdsFromVectorDB = List.of(1L, 10L, 20L);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), anyList()))
                    .willReturn(missionIdsFromVectorDB);

            DistanceMission mission1 = new DistanceMission(1000);
            ReflectionTestUtils.setField(mission1, "id", 1L);
            ReflectionTestUtils.setField(mission1, "title", "공원 1km 걷기");
            ReflectionTestUtils.setField(mission1, "category", MissionCategory.RELATIONSHIP);
            ReflectionTestUtils.setField(mission1, "difficulty", MissionDifficulty.EASY);

            TimerMission mission2 = new TimerMission(300);
            ReflectionTestUtils.setField(mission2, "id", 10L);
            ReflectionTestUtils.setField(mission2, "title", "5분 명상하기");
            ReflectionTestUtils.setField(mission2, "category", MissionCategory.COMMUNICATION);
            ReflectionTestUtils.setField(mission2, "difficulty", MissionDifficulty.NORMAL);

            TimerMission mission3 = new TimerMission(500);
            ReflectionTestUtils.setField(mission3, "id", 20L);
            ReflectionTestUtils.setField(mission3, "title", "5분 환기하기");
            ReflectionTestUtils.setField(mission3, "category", MissionCategory.ENVIRONMENT);
            ReflectionTestUtils.setField(mission3, "difficulty", MissionDifficulty.NORMAL);

            List<CloverMission> foundMissions = List.of(mission1, mission2, mission3);
            given(cloverMissionRepository.findAllById(missionIdsFromVectorDB))
                    .willReturn(foundMissions);

            List<CloverMissionRecord> savedMissions = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockMember))
                    .toList();
            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(savedMissions);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_MEMBER_ID);
            assertThat(result.getMissions().size()).isEqualTo(3);

            verify(cloverMissionVectorRepository, times(1)).searchSimilarMissionsIds(anyString(), eq(10), anyList());
            verify(cloverMissionRepository, times(1)).findAllById(missionIdsFromVectorDB);
            verify(cloverMissionRecordRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("실패 - VectorDB 조회 중 예외 발생 시 예외 전파")
        void getCloverMissionList_Fail_WhenVectorDbThrows() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(Collections.emptyList());

            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), anyList()))
                    .willThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    cloverMissionService.getCloverMissionList(TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(cloverMissionRepository, never()).findAllById(any());
            verify(cloverMissionRecordRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID로 조회")
        void getCloverMissionList_Fail_NotFound() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.getCloverMissionList(TEST_MEMBER_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("클로버 미션 상세 조회")
    class GetCloverMissionInfo {

        @Test
        @DisplayName("성공 - 거리(Distance) 미션")
        void getDistanceMissionInfo() {

            // --- Given ---
            CloverMissionRecord distanceMission = CloverMissionRecord.builder()
                    .member(mockMember)
                    .cloverType(CloverType.DISTANCE)
                    .requiredMeters(1000)
                    .progressInMeters(300)
                    .missionTitle("테스트 미션")
                    .build();
            ReflectionTestUtils.setField(distanceMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID))).willReturn(Optional.of(distanceMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(TEST_USER_MISSION_ID));

            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getUserMissionId()).isEqualTo(TEST_USER_MISSION_ID);
            assertThat(actualDto.getCloverType()).isEqualTo("DISTANCE");
        }

        @Test
        @DisplayName("성공 - 타이머(Timer) 미션")
        void getTimerMissionInfo() {

            // --- Given ---
            CloverMissionRecord timerMission = CloverMissionRecord.builder()
                    .member(mockMember)
                    .cloverType(CloverType.TIMER)
                    .requiredSeconds(600)
                    .progressInSeconds(200)
                    .build();
            ReflectionTestUtils.setField(timerMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID))).willReturn(Optional.of(timerMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(TEST_USER_MISSION_ID));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getUserMissionId()).isEqualTo(TEST_USER_MISSION_ID);
            assertThat(actualDto.getCloverType()).isEqualTo("TIMER");
        }

        @Test
        @DisplayName("성공 - 방문(Visit) 미션")
        void getVisitMissionInfo() {

            // --- Given ---
            String address = "서울시 강남구 테헤란로";
            CloverMissionRecord visitMission = CloverMissionRecord.builder()
                    .member(mockMember)
                    .cloverType(CloverType.VISIT)
                    .targetAddress(address)
                    .build();
            ReflectionTestUtils.setField(visitMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID))).willReturn(Optional.of(visitMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(TEST_USER_MISSION_ID));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getTargetAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("성공 - 사진 인증(Photo) 미션")
        void getPhotoMissionInfo() {

            // --- Given ---
            String imageUrl = "S3 URL";
            CloverMissionRecord photoMission = CloverMissionRecord.builder()
                    .member(mockMember)
                    .cloverType(CloverType.PHOTO)
                    .illustrationUrl(imageUrl)
                    .build();
            ReflectionTestUtils.setField(photoMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID))).willReturn(Optional.of(photoMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(TEST_USER_MISSION_ID));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getIllustrationUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 userMissionId로 조회 시 예외 발생")
        void getMissionInfo_NotFound_Failure() {

            // --- Given ---
            Long nonExistentId = 999L;
            given(cloverMissionRecordRepository.findByIdWithMember(eq(nonExistentId))).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.getCloverMissionInfo(nonExistentId, TEST_MEMBER_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(nonExistentId));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 미션 조회 시 403 에러")
        void getMissionInfo_Forbidden_Failure() {

            // --- Given ---
            Long otherMemberId = 99L;
            Member otherMember = Member.builder()
                    .email("other@example.com")
                    .oauthId("other-oauth")
                    .role(Role.USER)
                    .profile(new Profile("OtherUser", "https://example.com/other.jpg"))
                    .gender(Gender.MALE)
                    .build();
            ReflectionTestUtils.setField(otherMember, "id", otherMemberId);

            CloverMissionRecord someoneElseMission = CloverMissionRecord.builder()
                    .member(otherMember)
                    .cloverType(CloverType.TIMER)
                    .requiredSeconds(60)
                    .progressInSeconds(0)
                    .missionTitle("타 사용자 미션")
                    .build();
            ReflectionTestUtils.setField(someoneElseMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID)))
                    .willReturn(Optional.of(someoneElseMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
        }

        @Test
        @DisplayName("성공 - DISTANCE 타입 남은 거리 0으로 만들기")
        void getDistanceMissionInfo_RemainingDistanceClampedToZero() {

            // --- Given ---
            CloverMissionRecord distanceMission = CloverMissionRecord.builder()
                    .member(mockMember)
                    .cloverType(CloverType.DISTANCE)
                    .requiredMeters(500)
                    .progressInMeters(800)
                    .missionTitle("거리 초과 테스트")
                    .build();
            ReflectionTestUtils.setField(distanceMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID)))
                    .willReturn(Optional.of(distanceMission));

            // --- When ---
            CloverMissionResponseDto dto = cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(dto.getRemainingDistance()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - TIMER 타입 남은 시간 포맷 05:00")
        void getTimerMissionInfo_RemainingTimeFormatted() {

            // --- Given ---
            CloverMissionRecord timerMission = CloverMissionRecord.builder()
                    .member(mockMember)
                    .cloverType(CloverType.TIMER)
                    .requiredSeconds(400)
                    .progressInSeconds(100)
                    .missionTitle("타이머 포맷 테스트")
                    .build();
            ReflectionTestUtils.setField(timerMission, "id", TEST_USER_MISSION_ID);

            given(cloverMissionRecordRepository.findByIdWithMember(eq(TEST_USER_MISSION_ID)))
                    .willReturn(Optional.of(timerMission));

            // --- When ---
            CloverMissionResponseDto dto = cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(dto.getRemainingTime()).isEqualTo("05:00");
        }
    }

    @Nested
    @DisplayName("클로버 미션 상태 변경")
    class ChangeCloverCloverMissionStatus {

        @Test
        @DisplayName("성공 - 미션 상태 변경 (ASSIGNED -> STARTED)")
        void startCloverMission_Success() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.ASSIGNED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.startCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result.getMissionStatus()).isEqualTo(CloverMissionStatus.STARTED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 시작으로 변경(상태가 ASSIGNED, PAUSED 가 아님)")
        void startCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.STARTED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
        }

        @Test
        @DisplayName("실패 - 미션 상태 시작으로 변경(미션의 소유자가 아님)")
        void startCloverMission_Fail_Forbidden() {

            // --- Given ---
            Long anotherMemberId = 2L;

            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.ASSIGNED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(TEST_USER_MISSION_ID, anotherMemberId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션의 상태를 변경하려 함")
        void changeMissionStatus_Fail_MissionNotFound() {

            // --- Given ---
            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(999L, TEST_MEMBER_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
        }

        @Test
        @DisplayName("성공 - 미션 상태 일시정지로 변경(STARTED -> PAUSED)")
        void pauseCloverMission_Success() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.STARTED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.pauseCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result.getMissionStatus()).isEqualTo(CloverMissionStatus.PAUSED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 일시정지로 변경 (STARTED 상태가 아님)")
        void pauseCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.COMPLETED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.pauseCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
        }

        @Test
        @DisplayName("성공 - 미션 상태 완료 변경 (STARTED -> COMPLETED)")
        void completeCloverMission_Success() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.STARTED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.completeCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result.getMissionStatus()).isEqualTo(CloverMissionStatus.COMPLETED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 완료 변경 (STARTED 상태가 아님)")
        void completeCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(TEST_USER_MISSION_ID, CloverMissionStatus.PAUSED, mockMember);

            given(cloverMissionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.completeCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
        }
    }

    @Nested
    @DisplayName("클로버 미션 리필 (재할당)")
    class RefillCloverMissions {

        @Test
        @DisplayName("성공 - 기존 미션을 제외하고 새로운 미션을 재할당 받음")
        void assignCloverMissionList_Success() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));

            CloverMissionRecord existingRecord1 = createTestMissionRecord(201L, CloverMissionStatus.COMPLETED, mockMember);
            ReflectionTestUtils.setField(existingRecord1, "missionId", 101L);
            CloverMissionRecord existingRecord2 = createTestMissionRecord(202L, CloverMissionStatus.ASSIGNED, mockMember);
            ReflectionTestUtils.setField(existingRecord2, "missionId", 102L);

            List<CloverMissionRecord> todayMissions = List.of(existingRecord1, existingRecord2);
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(todayMissions);

            List<Long> excludedIds = List.of(101L, 102L);
            List<Long> newMissionIds = List.of(103L, 104L);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), eq(excludedIds)))
                    .willReturn(newMissionIds);

            CloverMission newMission1 = new TimerMission(300);
            ReflectionTestUtils.setField(newMission1, "id", 103L);
            CloverMission newMission2 = new TimerMission(600);
            ReflectionTestUtils.setField(newMission2, "id", 104L);
            List<CloverMission> foundMissions = List.of(newMission1, newMission2);

            given(cloverMissionRepository.findAllById(newMissionIds)).willReturn(foundMissions);

            List<CloverMissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockMember))
                    .toList();

            ReflectionTestUtils.setField(savedNewRecords.get(0), "id", 203L);
            ReflectionTestUtils.setField(savedNewRecords.get(1), "id", 204L);

            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(savedNewRecords);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_MEMBER_ID);
            assertThat(result.getMissions().size()).isEqualTo(2);
            assertThat(result.getMissions().get(0).getUserMissionId()).isEqualTo(203L);
            assertThat(result.getMissions().get(1).getUserMissionId()).isEqualTo(204L);
        }

        @Test
        @DisplayName("성공 - LLM 분석 결과를 활용한 미션 재할당")
        void assignCloverMissionList_Success_WithLLMAnalysis() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));

            CloverMissionRecord existingRecord = createTestMissionRecord(201L, CloverMissionStatus.COMPLETED, mockMember);
            ReflectionTestUtils.setField(existingRecord, "missionId", 101L);
            List<CloverMissionRecord> todayMissions = List.of(existingRecord);
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(todayMissions);

            List<UserFeedbackForLLMDto> mockFeedbacks = List.of(
                    UserFeedbackForLLMDto.builder()
                            .missionTitle("테스트 미션")
                            .feedbackDifficulty(MissionDifficulty.EASY)
                            .feedbackComment("쉬웠어요!")
                            .build()
            );
            given(cloverMissionRecordService.getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID))
                    .willReturn(mockFeedbacks);

            LLMProcessingResultDto llmResult = LLMProcessingResultDto.builder()
                    .searchQuery("가벼운 사회적 활동으로 자신감을 회복하고 싶어하는 상태")
                    .build();
            given(llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(mockFeedbacks))
                    .willReturn(llmResult);

            List<Long> excludedIds = List.of(101L);
            List<Long> newMissionIds = List.of(102L, 103L);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(
                    eq("가벼운 사회적 활동으로 자신감을 회복하고 싶어하는 상태"),
                    eq(10),
                    eq(excludedIds)))
                    .willReturn(newMissionIds);

            CloverMission newMission1 = new TimerMission(300);
            ReflectionTestUtils.setField(newMission1, "id", 102L);
            CloverMission newMission2 = new TimerMission(600);
            ReflectionTestUtils.setField(newMission2, "id", 103L);
            List<CloverMission> foundMissions = List.of(newMission1, newMission2);
            given(cloverMissionRepository.findAllById(newMissionIds)).willReturn(foundMissions);

            List<CloverMissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockMember))
                    .toList();
            ReflectionTestUtils.setField(savedNewRecords.get(0), "id", 202L);
            ReflectionTestUtils.setField(savedNewRecords.get(1), "id", 203L);
            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(savedNewRecords);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_MEMBER_ID);
            assertThat(result.getMissions().size()).isEqualTo(2);

            verify(cloverMissionRecordService).getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID);
            verify(llmBasedQueryGeneratorService).generateMissionRecommendationStrategy(mockFeedbacks);
            verify(cloverMissionVectorRepository).searchSimilarMissionsIds(
                    eq("가벼운 사회적 활동으로 자신감을 회복하고 싶어하는 상태"),
                    eq(10),
                    eq(excludedIds));
        }

        @Test
        @DisplayName("성공 - 최근 피드백이 없는 경우 기본 쿼리 사용")
        void assignCloverMissionList_Success_NoRecentFeedback() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));

            List<CloverMissionRecord> todayMissions = Collections.emptyList();
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(todayMissions);

            given(cloverMissionRecordService.getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID))
                    .willReturn(Collections.emptyList());

            List<Long> newMissionIds = List.of(101L, 102L);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(
                    eq("처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션"),
                    eq(10),
                    eq(Collections.emptyList())))
                    .willReturn(newMissionIds);

            CloverMission newMission1 = new TimerMission(300);
            ReflectionTestUtils.setField(newMission1, "id", 101L);
            CloverMission newMission2 = new TimerMission(600);
            ReflectionTestUtils.setField(newMission2, "id", 102L);
            List<CloverMission> foundMissions = List.of(newMission1, newMission2);
            given(cloverMissionRepository.findAllById(newMissionIds)).willReturn(foundMissions);

            List<CloverMissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockMember))
                    .toList();
            ReflectionTestUtils.setField(savedNewRecords.get(0), "id", 201L);
            ReflectionTestUtils.setField(savedNewRecords.get(1), "id", 202L);
            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(savedNewRecords);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_MEMBER_ID);
            assertThat(result.getMissions().size()).isEqualTo(2);

            verify(cloverMissionRecordService).getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID);
            verify(llmBasedQueryGeneratorService, never()).generateMissionRecommendationStrategy(any());
            verify(cloverMissionVectorRepository).searchSimilarMissionsIds(
                    eq("처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션"),
                    eq(10),
                    eq(Collections.emptyList()));
        }

        @Test
        @DisplayName("성공 - LLM 처리 실패 시 fallback 처리")
        void assignCloverMissionList_Success_LLMFallback() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));

            List<CloverMissionRecord> todayMissions = Collections.emptyList();
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(todayMissions);

            List<UserFeedbackForLLMDto> mockFeedbacks = List.of(
                    UserFeedbackForLLMDto.builder()
                            .missionTitle("테스트 미션")
                            .feedbackDifficulty(MissionDifficulty.HARD)
                            .feedbackComment("너무 어려웠어요!")
                            .build()
            );
            given(cloverMissionRecordService.getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID))
                    .willReturn(mockFeedbacks);

            // LLM 처리 실패 시뮬레이션
            given(llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(mockFeedbacks))
                    .willThrow(new RuntimeException("LLM 서비스 오류"));

            // Fallback 쿼리로 벡터 검색
            List<Long> newMissionIds = List.of(101L, 102L);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(
                    eq("처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션"),
                    eq(10),
                    eq(Collections.emptyList())))
                    .willReturn(newMissionIds);

            CloverMission newMission1 = new TimerMission(300);
            ReflectionTestUtils.setField(newMission1, "id", 101L);
            CloverMission newMission2 = new TimerMission(600);
            ReflectionTestUtils.setField(newMission2, "id", 102L);
            List<CloverMission> foundMissions = List.of(newMission1, newMission2);
            given(cloverMissionRepository.findAllById(newMissionIds)).willReturn(foundMissions);

            List<CloverMissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockMember))
                    .toList();
            ReflectionTestUtils.setField(savedNewRecords.get(0), "id", 201L);
            ReflectionTestUtils.setField(savedNewRecords.get(1), "id", 202L);
            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(savedNewRecords);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_MEMBER_ID);
            assertThat(result.getMissions().size()).isEqualTo(2);

            verify(cloverMissionRecordService).getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID);
            verify(llmBasedQueryGeneratorService).generateMissionRecommendationStrategy(mockFeedbacks);
            verify(cloverMissionVectorRepository).searchSimilarMissionsIds(
                    eq("처음 시작하는 사용자를 위한 가벼운 일상 활동과 간단한 사회적 소통 미션"),
                    eq(10),
                    eq(Collections.emptyList()));
        }

        @Test
        @DisplayName("성공 - 추천 가능한 미션이 3개 미만일 경우 가능한 만큼만 할당")
        void assignCloverMissionList_Success_WhenLessThanThreeMissionsAvailable() {
            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(cloverMissionRecordRepository.findCloverMissionsList(anyLong(), any())).willReturn(Collections.emptyList());

            // 벡터 DB에서 2개의 미션 ID만 반환
            List<Long> newMissionIds = List.of(101L, 102L);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), anyList()))
                    .willReturn(newMissionIds);

            CloverMission newMission1 = new TimerMission(300);
            ReflectionTestUtils.setField(newMission1, "id", 101L);
            CloverMission newMission2 = new TimerMission(600);
            ReflectionTestUtils.setField(newMission2, "id", 102L);
            List<CloverMission> foundMissions = List.of(newMission1, newMission2);
            given(cloverMissionRepository.findAllById(newMissionIds)).willReturn(foundMissions);

            List<CloverMissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockMember))
                    .toList();
            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(savedNewRecords);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            // 최종 할당된 미션은 2개여야 한다.
            assertThat(result.getMissions().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 추천 가능한 미션이 전혀 없을 경우 빈 리스트 반환")
        void assignCloverMissionList_Success_WhenNoMissionsAvailable() {
            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(cloverMissionRecordRepository.findCloverMissionsList(anyLong(), any())).willReturn(Collections.emptyList());

            // 벡터 DB에서 빈 리스트 반환
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), anyList()))
                    .willReturn(Collections.emptyList());

            given(cloverMissionRepository.findAllById(Collections.emptyList())).willReturn(Collections.emptyList());
            given(cloverMissionRecordRepository.saveAll(anyList())).willReturn(Collections.emptyList());

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            // 최종 할당된 미션은 0개여야 한다.
            assertThat(result.getMissions().size()).isEqualTo(0);
            verify(cloverMissionRecordRepository, times(1)).saveAll(eq(Collections.emptyList()));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자가 미션 재할당 요청")
        void assignCloverMissionList_Fail_UserNotFound() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - VectorDB 조회 중 예외 발생 시 예외 전파 및 저장 동작 없음")
        void assignCloverMissionList_Fail_WhenVectorDbThrows() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));

            CloverMissionRecord existingRecord1 = createTestMissionRecord(201L, CloverMissionStatus.ASSIGNED, mockMember);
            ReflectionTestUtils.setField(existingRecord1, "missionId", 101L);
            CloverMissionRecord existingRecord2 = createTestMissionRecord(202L, CloverMissionStatus.PAUSED, mockMember);
            ReflectionTestUtils.setField(existingRecord2, "missionId", 102L);
            CloverMissionRecord existingRecord3 = createTestMissionRecord(203L, CloverMissionStatus.COMPLETED, mockMember);
            ReflectionTestUtils.setField(existingRecord3, "missionId", 103L);

            List<CloverMissionRecord> existingTodayAllMissions = List.of(existingRecord1, existingRecord2, existingRecord3);
            given(cloverMissionRecordRepository.findCloverMissionsList(eq(TEST_MEMBER_ID), any(LocalDate.class)))
                    .willReturn(existingTodayAllMissions);

            List<Long> excludedMissionIds = existingTodayAllMissions.stream()
                    .map(CloverMissionRecord::getMissionId)
                    .toList();

            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), eq(excludedMissionIds)))
                    .willThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(cloverMissionRepository, never()).findAllById(any());
            verify(cloverMissionRecordRepository, never()).saveAll(anyList());
        }
    }

    /**
     * CloverMissionRecord 를 생성하는 헬퍼 메서드
     * @param userMissionId 미션 기록 ID
     * @param status 테스트에 필요한 미션 상태
     * @param member 미션 소유자
     * @return 생성된 CloverMissionRecord 객체
     */
    private CloverMissionRecord createTestMissionRecord(Long userMissionId, CloverMissionStatus status, Member member) {
        CloverMissionRecord mission = CloverMissionRecord.builder()
                .member(member)
                .cloverMissionStatus(status)
                .missionId(100L)
                .missionTitle("테스트 미션")
                .missionCategory(MissionCategory.RELATIONSHIP)
                .missionDifficulty(MissionDifficulty.EASY)
                .build();
        ReflectionTestUtils.setField(mission, "id", userMissionId);
        return mission;
    }

    @Nested
    @DisplayName("LLM 가중치 기반 미션 추천 정렬")
    class MissionWeightingAndSorting {

        @Test
        @DisplayName("성공 - LLM 추천(recommend) 전략에 따라 미션이 정렬되어 할당된다")
        void assignMissions_Success_SortedByRecommendedStrategy() {

            // --- Given ---
            CloverMission mission1 = createCloverMission(1L, "운동하기", MissionCategory.HEALTH, MissionDifficulty.EASY); // 점수: 10(카테고리)+10(난이도) = 20
            CloverMission mission2 = createCloverMission(2L, "친구에게 연락하기", MissionCategory.RELATIONSHIP, MissionDifficulty.EASY); // 점수: 10(난이도) = 10
            CloverMission mission3 = createCloverMission(3L, "10분 산책하기", MissionCategory.HEALTH, MissionDifficulty.NORMAL); // 점수: 10(카테고리) = 10
            CloverMission mission4 = createCloverMission(4L, "방 청소하기", MissionCategory.ENVIRONMENT, MissionDifficulty.HARD); // 점수: 0
            CloverMission mission5 = createCloverMission(5L, "새로운 장소 방문", MissionCategory.ENVIRONMENT, MissionDifficulty.NORMAL); // 점수: 0
            List<CloverMission> allMissions = List.of(mission1, mission2, mission3, mission4, mission5);
            List<Long> allMissionIds = allMissions.stream().map(CloverMission::getId).toList();

            LLMProcessingResultDto llmStrategy = LLMProcessingResultDto.builder()
                    .searchQuery("건강하고 쉬운 활동 추천")
                    .recommendCategories(List.of(MissionCategory.HEALTH))
                    .recommendDifficulties(List.of(MissionDifficulty.EASY))
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(cloverMissionRecordRepository.findCloverMissionsList(anyLong(), any())).willReturn(Collections.emptyList());
            given(cloverMissionRecordService.getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID)).willReturn(List.of(mock(UserFeedbackForLLMDto.class)));
            given(llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(anyList())).willReturn(llmStrategy);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), anyList())).willReturn(allMissionIds);
            given(cloverMissionRepository.findAllById(allMissionIds)).willReturn(allMissions);

            ArgumentCaptor<List<CloverMissionRecord>> captor = ArgumentCaptor.forClass(List.class);
            when(cloverMissionRecordRepository.saveAll(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            // --- When ---
            cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            List<CloverMissionRecord> savedRecords = captor.getValue();
            List<Long> savedMissionIds = savedRecords.stream().map(CloverMissionRecord::getMissionId).toList();

            assertThat(savedRecords.size()).isEqualTo(3);
            assertThat(savedMissionIds).contains(1L);
            assertThat(savedMissionIds).contains(2L, 3L);
        }

        @Test
        @DisplayName("성공 - LLM 회피(avoid) 전략에 따라 점수가 낮은 미션은 후순위로 밀려난다")
        void assignMissions_Success_SortedByAvoidedStrategy() {

            // --- Given ---
            CloverMission mission1 = createCloverMission(1L, "가벼운 스트레칭", MissionCategory.HEALTH, MissionDifficulty.EASY); // 점수: 10
            CloverMission mission2 = createCloverMission(2L, "명상하기", MissionCategory.HEALTH, MissionDifficulty.NORMAL); // 점수: 10
            CloverMission mission3 = createCloverMission(3L, "장보기", MissionCategory.ENVIRONMENT, MissionDifficulty.NORMAL); // 점수: 10-5 = 5
            CloverMission mission4 = createCloverMission(4L, "격렬한 운동", MissionCategory.HEALTH, MissionDifficulty.HARD); // 점수: 10-5 = 5
            CloverMission mission5 = createCloverMission(5L, "심부름하기", MissionCategory.ENVIRONMENT, MissionDifficulty.HARD); // 점수: -5
            List<CloverMission> allMissions = List.of(mission1, mission2, mission3, mission4, mission5);
            List<Long> allMissionIds = allMissions.stream().map(CloverMission::getId).toList();

            LLMProcessingResultDto llmStrategy = LLMProcessingResultDto.builder()
                    .searchQuery("건강하지만 어렵지 않은 활동 추천")
                    .recommendCategories(List.of(MissionCategory.HEALTH))
                    .avoidCategories(List.of(MissionCategory.ENVIRONMENT))
                    .avoidDifficulties(List.of(MissionDifficulty.HARD))
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(cloverMissionRecordRepository.findCloverMissionsList(anyLong(), any())).willReturn(Collections.emptyList());
            given(cloverMissionRecordService.getRecentMissionRecordsWithFeedback(TEST_MEMBER_ID)).willReturn(List.of(mock(UserFeedbackForLLMDto.class)));
            given(llmBasedQueryGeneratorService.generateMissionRecommendationStrategy(anyList())).willReturn(llmStrategy);
            given(cloverMissionVectorRepository.searchSimilarMissionsIds(anyString(), eq(10), anyList())).willReturn(allMissionIds);
            given(cloverMissionRepository.findAllById(allMissionIds)).willReturn(allMissions);

            ArgumentCaptor<List<CloverMissionRecord>> captor = ArgumentCaptor.forClass(List.class);
            when(cloverMissionRecordRepository.saveAll(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            // --- When ---
            cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID);

            // --- Then ---
            List<CloverMissionRecord> savedRecords = captor.getValue();
            List<Long> savedMissionIds = savedRecords.stream().map(CloverMissionRecord::getMissionId).toList();

            assertThat(savedRecords.size()).isEqualTo(3);
            assertThat(savedMissionIds).contains(1L, 2L);
            assertThat(savedMissionIds).doesNotContain(5L);
            assertThat(savedMissionIds).containsAnyOf(3L, 4L);
        }

        private CloverMission createCloverMission(Long id, String title, MissionCategory category, MissionDifficulty difficulty) {
            TimerMission mission = new TimerMission(300);
            ReflectionTestUtils.setField(mission, "id", id);
            ReflectionTestUtils.setField(mission, "title", title);
            ReflectionTestUtils.setField(mission, "category", category);
            ReflectionTestUtils.setField(mission, "difficulty", difficulty);
            return mission;
        }
    }
}