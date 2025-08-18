package com.example.live_backend.domain.mission.service;

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
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.entity.DistanceMission;
import com.example.live_backend.domain.mission.clover.entity.TimerMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.service.CloverMissionService;
import com.example.live_backend.domain.mission.clover.service.VectorDBService;
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

    private Member mockUser;
    private final Long userId = 1L;
    private final Long userMissionId = 10L;

    @BeforeEach
    void setUp() {

        mockUser = Member.builder()
                .email("mockuser@example.com")
                .oauthId("test-oauth-id")
                .role(Role.USER)
                .profile(new Profile("Mockuser", "https://example.com/profile.jpg"))
                .gender(Gender.FEMALE)
                .build();

        ReflectionTestUtils.setField(mockUser, "id", userId);
    }

    @Nested
    @DisplayName("클로버 미션 리스트 조회")
    class GetCloverMissionList {

        @Test
        @DisplayName("성공 - 오늘 생성된 클로버 미션이 이미 존재할 경우")
        void getCloverMissionList_Success_WhenMissionsExist() {

            // --- Given ---
            when(memberRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            List<CloverMissionRecord> existingMissions = List.of(
                    createTestMissionRecord(101L, CloverMissionStatus.ASSIGNED, mockUser),
                    createTestMissionRecord(102L, CloverMissionStatus.STARTED, mockUser),
                    createTestMissionRecord(103L, CloverMissionStatus.PAUSED, mockUser),
                    createTestMissionRecord(104L, CloverMissionStatus.COMPLETED, mockUser)
            );
            when(cloverMissionRecordRepository.findCloverMissionsList(eq(userId), any(LocalDate.class)))
                    .thenReturn(existingMissions);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList(userId);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getMissions().size()).isEqualTo(4);

            verify(vectorDBService, never()).searchSimilarMissionsIds(anyString(), anyInt(), anyList());
            verify(cloverMissionRepository, never()).findAllById(any());
            verify(cloverMissionRecordRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("성공 - 오늘 생성된 미션이 없어 새로 할당받는 경우")
        void getCloverMissionList_Success_WhenMissionsAreNewlyAssigned() {

            // --- Given ---
            when(memberRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(cloverMissionRecordRepository.findCloverMissionsList(eq(userId), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            List<Long> missionIdsFromVectorDB = List.of(1L, 2L);
            when(vectorDBService.searchSimilarMissionsIds(anyString(), anyInt(), anyList()))
                    .thenReturn(missionIdsFromVectorDB);

            DistanceMission mission1 = new DistanceMission(1000);
            ReflectionTestUtils.setField(mission1, "id", 1L);
            ReflectionTestUtils.setField(mission1, "title", "공원 1km 걷기");
            ReflectionTestUtils.setField(mission1, "category", MissionCategory.RELATIONSHIP);
            ReflectionTestUtils.setField(mission1, "difficulty", MissionDifficulty.EASY);

            TimerMission mission2 = new TimerMission(300);
            ReflectionTestUtils.setField(mission2, "id", 2L);
            ReflectionTestUtils.setField(mission2, "title", "5분 명상하기");
            ReflectionTestUtils.setField(mission2, "category", MissionCategory.COMMUNICATION);
            ReflectionTestUtils.setField(mission2, "difficulty", MissionDifficulty.NORMAL);

            List<CloverMission> foundMissions = List.of(mission1, mission2);
            when(cloverMissionRepository.findAllById(missionIdsFromVectorDB))
                    .thenReturn(foundMissions);

            List<CloverMissionRecord> savedMissions = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockUser))
                    .toList();
            when(cloverMissionRecordRepository.saveAll(anyList())).thenReturn(savedMissions);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList(userId);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getMissions().size()).isEqualTo(2);

            verify(vectorDBService, times(1)).searchSimilarMissionsIds(anyString(), eq(3), anyList());
            verify(cloverMissionRepository, times(1)).findAllById(missionIdsFromVectorDB);
            verify(cloverMissionRecordRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID로 조회")
        void getCloverMissionList_Fail_UserNotFound() {

            // --- Given ---
            when(memberRepository.findById(userId)).thenReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.getCloverMissionList(userId);
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
                    .member(mockUser)
                    .cloverType(CloverType.DISTANCE)
                    .requiredMeters(1000)
                    .progressInMeters(300)
                    .missionTitle("테스트 미션")
                    .build();
            ReflectionTestUtils.setField(distanceMission, "id", userMissionId);

            when(cloverMissionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(distanceMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(userMissionId));

            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getUserMissionId()).isEqualTo(userMissionId);
            assertThat(actualDto.getCloverType()).isEqualTo("DISTANCE");
        }

        @Test
        @DisplayName("성공 - 타이머(Timer) 미션")
        void getTimerMissionInfo() {

            // --- Given ---
            CloverMissionRecord timerMission = CloverMissionRecord.builder()
                    .member(mockUser)
                    .cloverType(CloverType.TIMER)
                    .requiredSeconds(600)
                    .progressInSeconds(200)
                    .build();
            ReflectionTestUtils.setField(timerMission, "id", userMissionId);

            when(cloverMissionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(timerMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(userMissionId));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getUserMissionId()).isEqualTo(userMissionId);
            assertThat(actualDto.getCloverType()).isEqualTo("TIMER");
        }

        @Test
        @DisplayName("성공 - 방문(Visit) 미션")
        void getVisitMissionInfo() {

            // --- Given ---
            String address = "서울시 강남구 테헤란로";
            CloverMissionRecord visitMission = CloverMissionRecord.builder()
                    .member(mockUser)
                    .cloverType(CloverType.VISIT)
                    .targetAddress(address)
                    .build();
            ReflectionTestUtils.setField(visitMission, "id", userMissionId);

            when(cloverMissionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(visitMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(userMissionId));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getTargetAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("성공 - 사진 인증(Photo) 미션")
        void getPhotoMissionInfo() {

            // --- Given ---
            String imageUrl = "S3 URL";
            CloverMissionRecord photoMission = CloverMissionRecord.builder()
                    .member(mockUser)
                    .cloverType(CloverType.PHOTO)
                    .illustrationUrl(imageUrl)
                    .build();
            ReflectionTestUtils.setField(photoMission, "id", userMissionId);

            when(cloverMissionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(photoMission));

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(userMissionId));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getIllustrationUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 userMissionId로 조회 시 예외 발생")
        void getMissionInfo_NotFound_Failure() {

            // --- Given ---
            Long nonExistentId = 999L;
            when(cloverMissionRecordRepository.findByIdWithMember(eq(nonExistentId))).thenReturn(Optional.empty());

            // --- When & Then ---
            CustomException e = assertThrows(CustomException.class, () -> {
                cloverMissionService.getCloverMissionInfo(nonExistentId, userId);
            });

            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(cloverMissionRecordRepository).findByIdWithMember(eq(nonExistentId));
        }
    }

    @Nested
    @DisplayName("클로버 미션 상태 변경")
    class ChangeCloverCloverMissionStatus {

        @Test
        @DisplayName("성공 - 미션 상태 변경 (ASSIGNED -> STARTED)")
        void startCloverMission_Success() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.ASSIGNED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.startCloverMission(userMissionId, userId);

            // --- Then ---
            assertThat(result.getMissionStatus()).isEqualTo(CloverMissionStatus.STARTED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 시작으로 변경(상태가 ASSIGNED, PAUSED 가 아님)")
        void startCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.STARTED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(userMissionId, userId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
        }

        @Test
        @DisplayName("실패 - 미션 상태 시작으로 변경(미션의 소유자가 아님)")
        void startCloverMission_Fail_Forbidden() {

            // --- Given ---
            Long anotherUserId = 2L;

            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.ASSIGNED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(userMissionId, anotherUserId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션의 상태를 변경하려 함")
        void changeMissionStatus_Fail_MissionNotFound() {

            // --- Given ---
            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(999L, userId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
        }

        @Test
        @DisplayName("성공 - 미션 상태 일시정지로 변경(STARTED -> PAUSED)")
        void pauseCloverMission_Success() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.STARTED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.pauseCloverMission(userMissionId, userId);

            // --- Then ---
            assertThat(result.getMissionStatus()).isEqualTo(CloverMissionStatus.PAUSED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 일시정지로 변경 (STARTED 상태가 아님)")
        void pauseCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.COMPLETED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.pauseCloverMission(userMissionId, userId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
        }

        @Test
        @DisplayName("성공 - 미션 상태 완료 변경 (STARTED -> COMPLETED)")
        void completeCloverMission_Success() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.STARTED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.completeCloverMission(userMissionId, userId);

            // --- Then ---
            assertThat(result.getMissionStatus()).isEqualTo(CloverMissionStatus.COMPLETED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 완료 변경 (STARTED 상태가 아님)")
        void completeCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            CloverMissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.PAUSED, mockUser);

            when(cloverMissionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.completeCloverMission(userMissionId, userId);
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
            when(memberRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            CloverMissionRecord existingRecord1 = createTestMissionRecord(201L, CloverMissionStatus.COMPLETED, mockUser);
            ReflectionTestUtils.setField(existingRecord1, "missionId", 101L);
            CloverMissionRecord existingRecord2 = createTestMissionRecord(202L, CloverMissionStatus.ASSIGNED, mockUser);
            ReflectionTestUtils.setField(existingRecord2, "missionId", 102L);

            List<CloverMissionRecord> todayMissions = List.of(existingRecord1, existingRecord2);
            when(cloverMissionRecordRepository.findCloverMissionsList(eq(userId), any(LocalDate.class)))
                    .thenReturn(todayMissions);

            List<Long> excludedIds = List.of(101L, 102L);
            List<Long> newMissionIds = List.of(103L, 104L);
            when(vectorDBService.searchSimilarMissionsIds(anyString(), anyInt(), eq(excludedIds)))
                    .thenReturn(newMissionIds);

            CloverMission newMission1 = new TimerMission(300);
            ReflectionTestUtils.setField(newMission1, "id", 103L);
            CloverMission newMission2 = new TimerMission(600);
            ReflectionTestUtils.setField(newMission2, "id", 104L);
            List<CloverMission> foundMissions = List.of(newMission1, newMission2);
            when(cloverMissionRepository.findAllById(newMissionIds)).thenReturn(foundMissions);

            List<CloverMissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> CloverMissionRecord.from(mission, mockUser))
                    .toList();
            when(cloverMissionRecordRepository.saveAll(anyList())).thenReturn(savedNewRecords);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.assignCloverMissionList(userId);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getMissions().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자가 미션 재할당 요청")
        void assignCloverMissionList_Fail_UserNotFound() {
            // --- Given ---
            when(memberRepository.findById(userId)).thenReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.assignCloverMissionList(userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * CloverMissionRecord 를 생성하는 헬퍼 메서드
     * @param userMissionId 미션 기록 ID
     * @param status 테스트에 필요한 미션 상태
     * @param user 미션 소유자
     * @return 생성된 CloverMissionRecord 객체
     */
    private CloverMissionRecord createTestMissionRecord(Long userMissionId, CloverMissionStatus status, Member user) {
        CloverMissionRecord mission = CloverMissionRecord.builder()
                .member(user)
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