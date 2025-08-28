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
import com.example.live_backend.domain.mission.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.entity.DistanceMission;
import com.example.live_backend.domain.mission.clover.entity.TimerMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private VectorDBService vectorDBService;

    @Mock
    private CloverMissionRepository cloverMissionRepository;

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

            verify(vectorDBService, never()).searchSimilarMissionsIds(anyString(), anyInt(), anyList());
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
            given(vectorDBService.searchSimilarMissionsIds(anyString(), anyInt(), anyList()))
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
            ReflectionTestUtils.setField(mission2, "id", 20L);
            ReflectionTestUtils.setField(mission2, "title", "5분 환기하기");
            ReflectionTestUtils.setField(mission2, "category", MissionCategory.ENVIRONMENT);
            ReflectionTestUtils.setField(mission2, "difficulty", MissionDifficulty.NORMAL);

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

            verify(vectorDBService, times(1)).searchSimilarMissionsIds(anyString(), eq(3), anyList());
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

            given(vectorDBService.searchSimilarMissionsIds(anyString(), eq(3), anyList()))
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

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

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
            given(vectorDBService.searchSimilarMissionsIds(anyString(), anyInt(), eq(excludedIds)))
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

            given(vectorDBService.searchSimilarMissionsIds(anyString(), anyInt(), eq(excludedMissionIds)))
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
}