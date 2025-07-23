package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.example.entity.User;
import com.example.live_backend.domain.memeber.util.UserUtil;
import com.example.live_backend.domain.mission.Enum.MissionCategory;
import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.Enum.MissionStatus;
import com.example.live_backend.domain.mission.Enum.MissionType;
import com.example.live_backend.domain.mission.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.mission.repository.MissionRecordRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private UserUtil userUtil;

    private User mockUser;
    private final Long userId = 1L;
    private final Long userMissionId = 10L;

    @BeforeEach
    void setUp() {

        // 테스트용 User 생성
        mockUser = User.builder()
            .email("mockuser@example.com")
            .socialProvider(User.SocialProvider.KAKAO)
            .socialId("mock-user-1")
            .nickname("Mock-user")
            .gender(User.Gender.FEMALE)
            .role(User.Role.MEMBER)
            .cloverBalance(100)
            .build();

        // mockUser 객체의 id 필드에 1L를 할당
        ReflectionTestUtils.setField(mockUser, "id", userId);
    }

    @Test
    @DisplayName("클로버 미션 리스트 조회 성공")
    void getCloverMissionList_Success() {

        // --- Given ---
        Long currentUserId = 1L;
        LocalDateTime now = LocalDateTime.now();

        List<MissionRecord> mockMissionsList = List.of(
                createTestMissionRecord(101L, MissionStatus.ASSIGNED, mockUser),
                createTestMissionRecord(102L, MissionStatus.STARTED, mockUser),
                createTestMissionRecord(103L, MissionStatus.PAUSED, mockUser),
                createTestMissionRecord(104L, MissionStatus.COMPLETED, mockUser)
        );

        when(userUtil.getCurrentUserId()).thenReturn(currentUserId);
        when(missionRecordRepository.findCloverMissions(eq(currentUserId), any(LocalDateTime.class)))
                .thenReturn(mockMissionsList);

        // --- When ---
        CloverMissionListResponseDto result = cloverMissionService.getCloverMissionList();

        // --- Then ---
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(currentUserId);
        assertThat(result.getMissions().size()).isEqualTo(4);
        assertThat(result.getMissions().get(0).getUserMissionId()).isEqualTo(101L);
        assertThat(result.getMissions().get(1).getUserMissionId()).isEqualTo(102L);
        assertThat(result.getMissions().get(2).getUserMissionId()).isEqualTo(103L);
        assertThat(result.getMissions().get(3).getUserMissionId()).isEqualTo(104L);

        assertThat(result.getMissions().get(0).getMissionStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(result.getMissions().get(1).getMissionStatus()).isEqualTo(MissionStatus.STARTED);
        assertThat(result.getMissions().get(2).getMissionStatus()).isEqualTo(MissionStatus.PAUSED);
        assertThat(result.getMissions().get(3).getMissionStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    @DisplayName("클로버 미션 리스트 조회 실패 - 미션 없음")
    void getCloverMissionList_Fail_MissionNotFound() {

        // --- Given ---
        Long currentUserId = 1L;
        when(userUtil.getCurrentUserId()).thenReturn(currentUserId);
        when(missionRecordRepository.findCloverMissions(eq(currentUserId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList()); // 빈 리스트 반환

        // --- When & Then ---
        CustomException exception = assertThrows(CustomException.class, () -> {
            cloverMissionService.getCloverMissionList();
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
    }

    @Test
    @DisplayName("미션 상태 변경 (ASSIGNED -> STARTED) 성공")
    void startCloverMission_Success() {

        // --- Given ---
        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.ASSIGNED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(userId);
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When ---
        CloverMissionStatusResponseDto result = cloverMissionService.startCloverMission(userMissionId);

        // --- Then ---
        // 반환된 객체의 상태가 STARTED 인지 확인
        assertThat(result.getMissionStatus()).isEqualTo(MissionStatus.STARTED);
    }

    @Test
    @DisplayName("미션 시작 실패 - 상태가 ASSIGNED, PAUSED 가 아님")
    void startCloverMission_Fail_InvalidStatus() {

        // --- Given ---
        // 미션 상태가 ASSIGNED, PAUSED 가 아닌 MissionRecord 생성
        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.STARTED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(userId);
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When & Then ---
        // 반환된 객체의 상태가 STARTED 인지 확인
        // CustomException 이 발생하는지, 그리고 ErrorCode 가 INVALID_MISSION_STATUS 인지 검증
        CustomException exception = assertThrows(CustomException.class, () -> {
            cloverMissionService.startCloverMission(userMissionId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
    }

    @Test
    @DisplayName("실패 - 미션 상태 시작으로 변경(미션의 소유자가 아님)")
    void startCloverMission_Fail_Forbidden() {

        // --- Given ---
        Long currentUserId = 2L; // 현재 로그인한 사용자 (1L이 아님)

        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.ASSIGNED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(currentUserId); // 현재 사용자는 1번 user
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When & Then ---
        CustomException exception = assertThrows(CustomException.class, () -> {
            cloverMissionService.startCloverMission(userMissionId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 미션의 상태를 변경하려 함")
    void changeMissionStatus_Fail_MissionNotFound() {

        // --- Given ---
        // findByIdWithUser가 어떤 ID로 호출되든 비어있는 Optional 을 반환하도록 설정
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.empty());

        // --- When & Then ---
        // 어떤 상태 변경을 시도하더라도 MISSION_NOT_FOUND 예외가 발생해야 한다
        CustomException exception = assertThrows(CustomException.class, () -> {
            cloverMissionService.startCloverMission(999L); // 존재하지 않는 ID
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
    }

    /**
     * MissionRecord 를 생성하는 헬퍼 메서드
     * @param userMissionId 미션 기록 ID
     * @param status 테스트에 필요한 미션 상태
     * @param user 미션 소유자
     * @return 생성된 MissionRecord 객체
     */
    private MissionRecord createTestMissionRecord(Long userMissionId, MissionStatus status, User user) {
        MissionRecord mission = MissionRecord.builder()
                .user(user)
                .missionStatus(status) // 파라미터로 받은 상태를 설정
                .missionType(MissionType.CLOVER)
                .missionId(100L)
                .missionTitle("테스트 미션")
                .missionCategory(MissionCategory.RELATIONSHIP)
                .missionDifficulty(MissionDifficulty.EASY)
                .build();
        ReflectionTestUtils.setField(mission, "id", userMissionId);
        return mission;
    }

    @Test
    @DisplayName("미션 일시정지 성공 (STARTED -> PAUSED)")
    void pauseCloverMission_Success() {

        // --- Given ---
        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.STARTED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(userId);
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When ---
        CloverMissionStatusResponseDto result = cloverMissionService.pauseCloverMission(userMissionId);

        // --- Then ---
        // 반환된 객체의 상태가 PAUSED 인지 확인
        assertThat(result.getMissionStatus()).isEqualTo(MissionStatus.PAUSED);
    }

    @Test
    @DisplayName("미션 일시정지 실패 - STARTED 상태가 아님")
    void pauseCloverMission_Fail_InvalidStatus() {

        // --- Given ---
        // 미션 상태가 STARTED 가 아닌 MissionRecord 생성
        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.COMPLETED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(userId);
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When & Then ---
        // CustomException 이 발생하는지, 그리고 ErrorCode 가 INVALID_MISSION_STATUS 인지 검증
        CustomException exception = assertThrows(CustomException.class, () -> {
            cloverMissionService.startCloverMission(userMissionId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
    }

    @Test
    @DisplayName("미션 완료 성공 (STARTED -> COMPLETED)")
    void completeCloverMission_Success() {

        // --- Given ---
        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.STARTED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(userId);
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When ---
        CloverMissionStatusResponseDto result = cloverMissionService.completeCloverMission(userMissionId);

        // --- Then ---
        // 반환된 객체의 상태가 COMPLETED 인지 확인
        assertThat(result.getMissionStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    @DisplayName("미션 완료 실패 - STARTED 상태가 아님")
    void completeCloverMission_Fail_InvalidStatus() {

        // --- Given ---
        // 미션 상태가 STARTED 가 아닌 MissionRecord 생성
        MissionRecord assignedMission = createTestMissionRecord(userMissionId, MissionStatus.PAUSED, mockUser);

        when(userUtil.getCurrentUserId()).thenReturn(userId);
        when(missionRecordRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(assignedMission));

        // --- When & Then ---
        // CustomException 이 발생하는지, 그리고 ErrorCode 가 INVALID_MISSION_STATUS 인지 검증
        CustomException exception = assertThrows(CustomException.class, () -> {
            cloverMissionService.completeCloverMission(userMissionId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
    }
}