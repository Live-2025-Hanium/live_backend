package com.example.live_backend.domain.mission.my.service;

import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.my.Enum.MyMissionStatus;
import com.example.live_backend.domain.mission.my.Enum.RepeatType;
import com.example.live_backend.domain.mission.my.dto.MyMissionRecordResponseDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionResponseDto;
import com.example.live_backend.domain.mission.my.entity.MyMission;
import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import com.example.live_backend.domain.mission.my.repository.MyMissionRecordRepository;
import com.example.live_backend.domain.mission.my.repository.MyMissionRepository;
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("마이 미션 서비스 테스트")
class MyMissionServiceTest {

    @InjectMocks
    private MyMissionService myMissionService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MyMissionRepository myMissionRepository;

    @Mock
    private MyMissionRecordRepository myMissionRecordRepository;

    private Member mockMember;
    private MyMission mockMyMission;
    private MyMissionRecord mockMyMissionRecord;
    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_MY_MISSION_ID = 10L;
    private final Long TEST_USER_MISSION_ID = 20L;

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

        mockMyMission = MyMission.builder()
                .member(mockMember)
                .title("테스트 마이미션")
                .isActive(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .scheduledTime(LocalTime.of(9, 0))
                .repeatType(RepeatType.EVERYDAY)
                .build();

        ReflectionTestUtils.setField(mockMyMission, "id", TEST_MY_MISSION_ID);

        mockMyMissionRecord = MyMissionRecord.builder()
                .myMission(mockMyMission)
                .member(mockMember)
                .assignedDate(LocalDate.now())
                .myMissionStatus(MyMissionStatus.ASSIGNED)
                .build();

        ReflectionTestUtils.setField(mockMyMissionRecord, "id", TEST_USER_MISSION_ID);
    }

    @Nested
    @DisplayName("마이미션 생성")
    class CreateMyMission {

        @Test
        @DisplayName("성공 - 유효한 요청으로 마이미션 생성")
        void createMyMission_Success_ValidRequest() {

            // --- Given ---
            MyMissionRequestDto requestDto = MyMissionRequestDto.builder()
                    .missionTitle("새로운 마이미션")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(7))
                    .scheduledTime(LocalTime.of(9, 0))
                    .repeatType(RepeatType.EVERYDAY)
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(myMissionRepository.save(any(MyMission.class))).willReturn(mockMyMission);

            // --- When ---
            MyMissionResponseDto result = myMissionService.createMyMission(requestDto, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            verify(memberRepository).findById(TEST_MEMBER_ID);
            verify(myMissionRepository).save(any(MyMission.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void createMyMission_Fail_UserNotFound() {

            // --- Given ---
            MyMissionRequestDto requestDto = MyMissionRequestDto.builder()
                    .missionTitle("새로운 마이미션")
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.createMyMission(requestDto, TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(memberRepository).findById(TEST_MEMBER_ID);
            verify(myMissionRepository, never()).save(any(MyMission.class));
        }
    }

    @Nested
    @DisplayName("마이미션 수정")
    class UpdateMyMission {

        @Test
        @DisplayName("성공 - 본인의 마이미션 수정")
        void updateMyMission_Success_OwnMission() {

            // --- Given ---
            MyMissionRequestDto requestDto = MyMissionRequestDto.builder()
                    .missionTitle("수정된 마이미션")
                    .build();

            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When ---
            MyMissionResponseDto result = myMissionService.updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션")
        void updateMyMission_Fail_MissionNotFound() {

            // --- Given ---
            MyMissionRequestDto requestDto = MyMissionRequestDto.builder()
                    .missionTitle("수정된 마이미션")
                    .build();

            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 미션 수정 시도")
        void updateMyMission_Fail_UnauthorizedUser() {

            // --- Given ---
            Long otherUserId = 999L;
            MyMissionRequestDto requestDto = MyMissionRequestDto.builder()
                    .missionTitle("수정된 마이미션")
                    .build();

            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.updateMyMission(TEST_MY_MISSION_ID, requestDto, otherUserId));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_UPDATE_DENIED);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }
    }

    @Nested
    @DisplayName("마이미션 삭제")
    class DeleteMyMission {

        @Test
        @DisplayName("성공 - 본인의 마이미션 삭제")
        void deleteMyMission_Success_OwnMission() {

            // --- Given ---
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));
            doNothing().when(myMissionRepository).delete(mockMyMission);

            // --- When ---
            myMissionService.deleteMyMission(TEST_MY_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
            verify(myMissionRepository).delete(mockMyMission);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션 삭제")
        void deleteMyMission_Fail_MissionNotFound() {

            // --- Given ---
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.deleteMyMission(TEST_MY_MISSION_ID, TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
            verify(myMissionRepository, never()).delete(any(MyMission.class));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 미션 삭제 시도")
        void deleteMyMission_Fail_UnauthorizedUser() {

            // --- Given ---
            Long otherUserId = 999L;
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.deleteMyMission(TEST_MY_MISSION_ID, otherUserId));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_DELETE_DENIED);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
            verify(myMissionRepository, never()).delete(any(MyMission.class));
        }
    }

    @Nested
    @DisplayName("마이미션 목록 조회")
    class GetMyMissionsList {

        @Test
        @DisplayName("성공 - 사용자의 마이미션 목록 조회")
        void getMyMissionsList_Success() {

            // --- Given ---
            List<MyMission> mockMissions = List.of(mockMyMission);
            given(myMissionRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(mockMissions);

            // --- When ---
            List<MyMissionResponseDto> result = myMissionService.getMyMissionsList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);
            verify(myMissionRepository).findAllByMemberId(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 빈 목록 반환")
        void getMyMissionsList_Success_EmptyList() {

            // --- Given ---
            given(myMissionRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(Collections.emptyList());

            // --- When ---
            List<MyMissionResponseDto> result = myMissionService.getMyMissionsList(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(0);
            verify(myMissionRepository).findAllByMemberId(TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("오늘 수행할 마이미션 조회")
    class GetTodayMissions {

        @Test
        @DisplayName("성공 - 기존 미션 레코드가 있는 경우")
        void getTodayMissions_Success_ExistingRecords() {

            // --- Given ---
            LocalDate today = LocalDate.now();
            List<MyMissionRecord> existingRecords = List.of(mockMyMissionRecord);

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(myMissionRecordRepository.findByMemberAndAssignedDate(mockMember, today))
                    .willReturn(existingRecords);
            given(myMissionRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(Collections.emptyList());

            // --- When ---
            List<MyMissionRecordResponseDto> result = myMissionService.getTodayMissions(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);
            verify(memberRepository).findById(TEST_MEMBER_ID);
            verify(myMissionRecordRepository, times(2)).findByMemberAndAssignedDate(mockMember, today);
        }

        @Test
        @DisplayName("성공 - 새로운 미션 레코드 생성")
        void getTodayMissions_Success_CreateNewRecords() {

            // --- Given ---
            LocalDate today = LocalDate.now();
            List<MyMission> activeMissions = List.of(mockMyMission);

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(mockMember));
            given(myMissionRecordRepository.findByMemberAndAssignedDate(mockMember, today))
                    .willReturn(Collections.emptyList())
                    .willReturn(List.of(mockMyMissionRecord));
            given(myMissionRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(activeMissions);
            given(myMissionRecordRepository.saveAll(anyList())).willReturn(List.of(mockMyMissionRecord));

            // --- When ---
            List<MyMissionRecordResponseDto> result = myMissionService.getTodayMissions(TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            verify(memberRepository).findById(TEST_MEMBER_ID);
            verify(myMissionRecordRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void getTodayMissions_Fail_UserNotFound() {

            // --- Given ---
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.getTodayMissions(TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(memberRepository).findById(TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("마이미션 완료 처리")
    class CompleteMyMission {

        @Test
        @DisplayName("성공 - 오늘 할당된 미션 완료")
        void completeMyMission_Success() {

            // --- Given ---
            LocalDate today = LocalDate.now();
            ReflectionTestUtils.setField(mockMyMissionRecord, "assignedDate", today);

            given(myMissionRecordRepository.findById(TEST_USER_MISSION_ID)).willReturn(Optional.of(mockMyMissionRecord));
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When ---
            MyMissionRecordResponseDto result = myMissionService.completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);

            // --- Then ---
            assertThat(result).isNotNull();
            verify(myMissionRecordRepository).findById(TEST_USER_MISSION_ID);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션 레코드")
        void completeMyMission_Fail_RecordNotFound() {

            // --- Given ---
            given(myMissionRecordRepository.findById(TEST_USER_MISSION_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(myMissionRecordRepository).findById(TEST_USER_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 만료된 미션 완료 시도")
        void completeMyMission_Fail_ExpiredMission() {

            // --- Given ---
            LocalDate yesterday = LocalDate.now().minusDays(1);
            ReflectionTestUtils.setField(mockMyMissionRecord, "assignedDate", yesterday);

            given(myMissionRecordRepository.findById(TEST_USER_MISSION_ID)).willReturn(Optional.of(mockMyMissionRecord));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_EXPIRED);
            verify(myMissionRecordRepository).findById(TEST_USER_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 미션 완료 시도")
        void completeMyMission_Fail_UnauthorizedUser() {

            // --- Given ---
            Long otherUserId = 999L;
            LocalDate today = LocalDate.now();
            ReflectionTestUtils.setField(mockMyMissionRecord, "assignedDate", today);

            given(myMissionRecordRepository.findById(TEST_USER_MISSION_ID)).willReturn(Optional.of(mockMyMissionRecord));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.completeMyMission(TEST_USER_MISSION_ID, otherUserId));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
            verify(myMissionRecordRepository).findById(TEST_USER_MISSION_ID);
        }
    }

    @Nested
    @DisplayName("마이미션 활성화 상태 변경")
    class ChangeActive {

        @Test
        @DisplayName("성공 - 미션 활성화")
        void changeActive_Success_Activate() {

            // --- Given ---
            boolean active = true;
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When ---
            MyMissionResponseDto result = myMissionService.changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active);

            // --- Then ---
            assertThat(result).isNotNull();
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }

        @Test
        @DisplayName("성공 - 미션 비활성화")
        void changeActive_Success_Deactivate() {

            // --- Given ---
            boolean active = false;
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When ---
            MyMissionResponseDto result = myMissionService.changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active);

            // --- Then ---
            assertThat(result).isNotNull();
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션")
        void changeActive_Fail_MissionNotFound() {

            // --- Given ---
            boolean active = true;
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.empty());

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 미션 상태 변경 시도")
        void changeActive_Fail_UnauthorizedUser() {

            // --- Given ---
            Long otherUserId = 999L;
            boolean active = true;
            given(myMissionRepository.findById(TEST_MY_MISSION_ID)).willReturn(Optional.of(mockMyMission));

            // --- When & Then ---
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionService.changeActive(otherUserId, TEST_MY_MISSION_ID, active));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_UPDATE_DENIED);
            verify(myMissionRepository).findById(TEST_MY_MISSION_ID);
        }
    }
}