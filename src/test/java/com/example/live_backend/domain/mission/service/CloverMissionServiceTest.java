package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.Enum.*;
import com.example.live_backend.domain.mission.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.entity.CloverMission;
import com.example.live_backend.domain.mission.entity.DistanceMission;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.mission.entity.TimerMission;
import com.example.live_backend.domain.mission.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.repository.MissionRecordRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("클로버 미션 서비스 테스트")
class CloverMissionServiceTest {

    @InjectMocks
    private CloverMissionService cloverMissionService;

    @Mock
    private MissionRecordRepository missionRecordRepository;

    @Mock
    private CloverMissionDtoConverter cloverMissionDtoConverter;

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

        // mockUser 객체의 id 필드에 1L를 할당
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

            List<MissionRecord> existingMissions = List.of(
                    createTestMissionRecord(101L, CloverMissionStatus.ASSIGNED, mockUser),
                    createTestMissionRecord(102L, CloverMissionStatus.STARTED, mockUser),
                    createTestMissionRecord(103L, CloverMissionStatus.PAUSED, mockUser),
                    createTestMissionRecord(104L, CloverMissionStatus.COMPLETED, mockUser)
            );
            when(missionRecordRepository.findCloverMissions(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(existingMissions);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList(userId);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getMissions().size()).isEqualTo(4);

            verify(vectorDBService, never()).searchSimilarMissionsIds(anyString(), anyInt(), anyList());
            verify(cloverMissionRepository, never()).findAllById(any());
            verify(missionRecordRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("성공 - 오늘 생성된 미션이 없어 새로 할당받는 경우")
        void getCloverMissionList_Success_WhenMissionsAreNewlyAssigned() {

            // --- Given ---
            when(memberRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            when(missionRecordRepository.findCloverMissions(eq(userId), any(LocalDateTime.class)))
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

            List<MissionRecord> savedMissions = foundMissions.stream()
                    .map(mission -> MissionRecord.fromCloverMission(mission, mockUser))
                    .toList();
            when(missionRecordRepository.saveAll(anyList())).thenReturn(savedMissions);

            // --- When ---
            CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList(userId);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getMissions().size()).isEqualTo(2);

            verify(vectorDBService, times(1)).searchSimilarMissionsIds(anyString(), eq(3), anyList());
            verify(cloverMissionRepository, times(1)).findAllById(missionIdsFromVectorDB);
            verify(missionRecordRepository, times(1)).saveAll(anyList());
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
            MissionRecord distanceMission = MissionRecord.builder()
                    .id(userMissionId)
                    .member(mockUser)
                    .cloverType(CloverType.DISTANCE)
                    .requiredMeters(1000)
                    .progressInMeters(300)
                    .missionTitle("테스트 미션")
                    .build();

            when(missionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(distanceMission));

            CloverMissionResponseDto expectedDto = CloverMissionResponseDto.builder()
                    .userMissionId(userMissionId)
                    .cloverType("DISTANCE")
                    .build();
            when(cloverMissionDtoConverter.convert(any(MissionRecord.class))).thenReturn(expectedDto);


            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(missionRecordRepository).findByIdWithMember(eq(userMissionId));
            verify(cloverMissionDtoConverter).convert(eq(distanceMission));

            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getUserMissionId()).isEqualTo(userMissionId);
            assertThat(actualDto.getCloverType()).isEqualTo("DISTANCE");
        }

        @Test
        @DisplayName("성공 - 타이머(Timer) 미션")
        void getTimerMissionInfo() {

            // --- Given ---
            MissionRecord timerMission = MissionRecord.builder()
                    .id(userMissionId)
                    .member(mockUser)
                    .cloverType(CloverType.TIMER)
                    .requiredSeconds(600)
                    .progressInSeconds(200)
                    .build();

            CloverMissionResponseDto expectedDto = CloverMissionResponseDto.builder()
                    .userMissionId(userMissionId)
                    .cloverType("TIMER")
                    .build();

            when(missionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(timerMission));
            when(cloverMissionDtoConverter.convert(any(MissionRecord.class))).thenReturn(expectedDto);

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(missionRecordRepository).findByIdWithMember(eq(userMissionId));
            verify(cloverMissionDtoConverter).convert(eq(timerMission));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getUserMissionId()).isEqualTo(userMissionId);
        }

        @Test
        @DisplayName("성공 - 방문(Visit) 미션")
        void getVisitMissionInfo() {

            // --- Given ---
            String address = "서울시 강남구 테헤란로";
            MissionRecord visitMission = MissionRecord.builder()
                    .id(userMissionId)
                    .member(mockUser)
                    .cloverType(CloverType.VISIT)
                    .targetAddress(address)
                    .build();

            CloverMissionResponseDto expectedDto = CloverMissionResponseDto.builder()
                    .userMissionId(userMissionId)
                    .cloverType("VISIT")
                    .targetAddress(address)
                    .build();

            when(missionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(visitMission));
            when(cloverMissionDtoConverter.convert(any(MissionRecord.class))).thenReturn(expectedDto);

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(missionRecordRepository).findByIdWithMember(eq(userMissionId));
            verify(cloverMissionDtoConverter).convert(eq(visitMission));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getTargetAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("성공 - 사진 인증(Photo) 미션")
        void getPhotoMissionInfo() {

            // --- Given ---
            String imageUrl = "S3 URL";
            MissionRecord photoMission = MissionRecord.builder()
                    .id(userMissionId)
                    .member(mockUser) // 소유자 정보 추가
                    .cloverType(CloverType.PHOTO)
                    .illustrationUrl(imageUrl)
                    .build();

            CloverMissionResponseDto expectedDto = CloverMissionResponseDto.builder()
                    .userMissionId(userMissionId)
                    .cloverType("PHOTO")
                    .illustrationUrl(imageUrl)
                    .build();

            when(missionRecordRepository.findByIdWithMember(eq(userMissionId))).thenReturn(Optional.of(photoMission));
            when(cloverMissionDtoConverter.convert(any(MissionRecord.class))).thenReturn(expectedDto);

            // --- When ---
            CloverMissionResponseDto actualDto = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

            // --- Then ---
            verify(missionRecordRepository).findByIdWithMember(eq(userMissionId));
            verify(cloverMissionDtoConverter).convert(eq(photoMission));
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.getIllustrationUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 userMissionId로 조회 시 예외 발생")
        void getMissionInfo_NotFound_Failure() {

            // --- Given ---
            Long nonExistentId = 999L;
            when(missionRecordRepository.findByIdWithMember(eq(nonExistentId))).thenReturn(Optional.empty());

            // --- When & Then ---
            CustomException e = assertThrows(CustomException.class, () -> {
                cloverMissionService.getCloverMissionInfo(nonExistentId, userId);
            });

            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(missionRecordRepository).findByIdWithMember(eq(nonExistentId));
        }
    }

    @Nested
    @DisplayName("클로버 미션 상태 변경")
    class ChangeCloverCloverMissionStatus {

        @Test
        @DisplayName("성공 - 미션 상태 변경 (ASSIGNED -> STARTED)")
        void startCloverMission_Success() {

            // --- Given ---
            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.ASSIGNED, mockUser);

            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.startCloverMission(userMissionId, userId);

            // --- Then ---
            assertThat(result.getCloverMissionStatus()).isEqualTo(CloverMissionStatus.STARTED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 시작으로 변경(상태가 ASSIGNED, PAUSED 가 아님)")
        void startCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.STARTED, mockUser);

            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

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
            Long anotherUserId = 2L; // 현재 로그인한 사용자 (1L이 아님)

            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.ASSIGNED, mockUser);

            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

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
            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(999L, userId); // 존재하지 않는 ID
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
        }

        @Test
        @DisplayName("성공 - 미션 상태 일시정지로 변경(STARTED -> PAUSED)")
        void pauseCloverMission_Success() {

            // --- Given ---
            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.STARTED, mockUser);

            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.pauseCloverMission(userMissionId, userId);

            // --- Then ---
            assertThat(result.getCloverMissionStatus()).isEqualTo(CloverMissionStatus.PAUSED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 일시정지로 변경 (STARTED 상태가 아님)")
        void pauseCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.COMPLETED, mockUser);

            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () -> {
                cloverMissionService.startCloverMission(userMissionId, userId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
        }

        @Test
        @DisplayName("성공 - 미션 상태 완료 변경 (STARTED -> COMPLETED)")
        void completeCloverMission_Success() {

            // --- Given ---
            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.STARTED, mockUser);

            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

            // --- When ---
            CloverMissionStatusResponseDto result = cloverMissionService.completeCloverMission(userMissionId, userId);

            // --- Then ---
            assertThat(result.getCloverMissionStatus()).isEqualTo(CloverMissionStatus.COMPLETED);
        }

        @Test
        @DisplayName("실패 - 미션 상태 완료 변경 (STARTED 상태가 아님)")
        void completeCloverMission_Fail_InvalidStatus() {

            // --- Given ---
            MissionRecord assignedMission = createTestMissionRecord(userMissionId, CloverMissionStatus.PAUSED, mockUser);


            when(missionRecordRepository.findByIdWithMember(anyLong())).thenReturn(Optional.of(assignedMission));

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

            MissionRecord existingRecord1 = createTestMissionRecord(201L, CloverMissionStatus.COMPLETED, mockUser);
            ReflectionTestUtils.setField(existingRecord1, "missionId", 101L);
            MissionRecord existingRecord2 = createTestMissionRecord(202L, CloverMissionStatus.ASSIGNED, mockUser);
            ReflectionTestUtils.setField(existingRecord2, "missionId", 102L);

            List<MissionRecord> todayMissions = List.of(existingRecord1, existingRecord2);
            when(missionRecordRepository.findCloverMissions(eq(userId), any(LocalDateTime.class)))
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

            List<MissionRecord> savedNewRecords = foundMissions.stream()
                    .map(mission -> MissionRecord.fromCloverMission(mission, mockUser))
                    .toList();
            when(missionRecordRepository.saveAll(anyList())).thenReturn(savedNewRecords);

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
     * MissionRecord 를 생성하는 헬퍼 메서드
     * @param userMissionId 미션 기록 ID
     * @param status 테스트에 필요한 미션 상태
     * @param user 미션 소유자
     * @return 생성된 MissionRecord 객체
     */
        private MissionRecord createTestMissionRecord(Long userMissionId, CloverMissionStatus status, Member user) {
        MissionRecord mission = MissionRecord.builder()
                .member(user)
                .cloverMissionStatus(status) // 파라미터로 받은 상태를 설정
                .missionType(MissionType.CLOVER)
                .missionId(100L)
                .missionTitle("테스트 미션")
                .missionCategory(MissionCategory.RELATIONSHIP)
                .missionDifficulty(MissionDifficulty.EASY)
                .build();
        ReflectionTestUtils.setField(mission, "id", userMissionId);
        return mission;
    }
}