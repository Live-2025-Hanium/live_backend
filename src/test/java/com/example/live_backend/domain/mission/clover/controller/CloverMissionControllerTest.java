package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverMissionService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloverMissionController 테스트")
class CloverMissionControllerTest {

    @Mock
    private CloverMissionService cloverMissionService;

    @Mock
    private PrincipalDetails principalDetails;

    @InjectMocks
    private CloverMissionController cloverMissionController;

    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_USER_MISSION_ID = 100L;

    @Nested
    @DisplayName("GET /api/v1/missions/clover")
    class GetCloverMissionListTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 클로버 미션 목록 조회")
        void getCloverMissionList_AuthenticatedUser_Success() {

            // Given
            CloverMissionListResponseDto mockResponse = new CloverMissionListResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.getCloverMissionList(TEST_MEMBER_ID)).willReturn(mockResponse);

            // When
            ResponseHandler<CloverMissionListResponseDto> response =
                    cloverMissionController.getCloverMissionList(principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverMissionService).getCloverMissionList(TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/missions/clover/{userMissionId}")
    class GetCloverMissionInfoTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 미션 상세 조회")
        void getCloverMissionInfo_AuthenticatedUser_Success() {

            // Given
            CloverMissionResponseDto mockResponse = new CloverMissionResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willReturn(mockResponse);

            // When
            ResponseHandler<CloverMissionResponseDto> response = 
                cloverMissionController.getCloverMissionInfo(TEST_USER_MISSION_ID, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverMissionService).getCloverMissionInfo(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션 조회 시 404 에러")
        void getCloverMissionInfo_NonExistentMission_ThrowsNotFound() {

            // Given
            Long nonExistentMissionId = 999L;
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.getCloverMissionInfo(nonExistentMissionId, TEST_MEMBER_ID))
                .willThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> 
                cloverMissionController.getCloverMissionInfo(nonExistentMissionId, principalDetails));

            assertEquals(ErrorCode.MISSION_NOT_FOUND, exception.getErrorCode());
            verify(cloverMissionService).getCloverMissionInfo(nonExistentMissionId, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 미션 조회 시도 시 403 에러")
        void getCloverMissionInfo_OtherUserMission_ThrowsForbidden() {

            // Given
            Long otherUserMissionId = 200L;
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.getCloverMissionInfo(otherUserMissionId, TEST_MEMBER_ID))
                .willThrow(new CustomException(ErrorCode.MISSION_FORBIDDEN));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> 
                cloverMissionController.getCloverMissionInfo(otherUserMissionId, principalDetails));
            
            assertEquals(ErrorCode.MISSION_FORBIDDEN, exception.getErrorCode());
            verify(cloverMissionService).getCloverMissionInfo(otherUserMissionId, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/missions/clover/{userMissionId}/start")
    class StartMissionTests {

        @Test
        @DisplayName("성공 - ASSIGNED 상태의 미션 시작")
        void startMission_AssignedStatus_Success() {

            // Given
            CloverMissionStatusResponseDto mockResponse = new CloverMissionStatusResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.startCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willReturn(mockResponse);

            // When
            ResponseHandler<CloverMissionStatusResponseDto> response = 
                cloverMissionController.startMission(TEST_USER_MISSION_ID, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverMissionService).startCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 상태가 ASSIGNED와 PAUSED가 아닌 미션 시작 시도 시 400 에러")
        void startMission_AlreadyInStarted_ThrowsBadRequest() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.startCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willThrow(new CustomException(ErrorCode.INVALID_MISSION_STATUS));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> 
                cloverMissionController.startMission(TEST_USER_MISSION_ID, principalDetails));
            
            assertEquals(ErrorCode.INVALID_MISSION_STATUS, exception.getErrorCode());
            verify(cloverMissionService).startCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션 시작 시도 시 404 에러")
        void startMission_NonExistentMission_ThrowsNotFound() {

            // Given
            Long nonExistentMissionId = 999L;
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.startCloverMission(nonExistentMissionId, TEST_MEMBER_ID))
                .willThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> 
                cloverMissionController.startMission(nonExistentMissionId, principalDetails));
            
            assertEquals(ErrorCode.MISSION_NOT_FOUND, exception.getErrorCode());
            verify(cloverMissionService).startCloverMission(nonExistentMissionId, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/missions/clover/{userMissionId}/pause")
    class PauseMissionTests {

        @Test
        @DisplayName("성공 - STARTED 상태의 미션 일시정지")
        void pauseMission_StartedStatus_Success() {

            // Given
            CloverMissionStatusResponseDto mockResponse = new CloverMissionStatusResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.pauseCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willReturn(mockResponse);

            // When
            ResponseHandler<CloverMissionStatusResponseDto> response = 
                cloverMissionController.pauseMission(TEST_USER_MISSION_ID, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverMissionService).pauseCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - STARTED 상태가 아닌 미션 일시정지 시도 시 400 에러")
        void pauseMission_ReadyStatus_ThrowsBadRequest() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.pauseCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willThrow(new CustomException(ErrorCode.INVALID_MISSION_STATUS));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> 
                cloverMissionController.pauseMission(TEST_USER_MISSION_ID, principalDetails));
            
            assertEquals(ErrorCode.INVALID_MISSION_STATUS, exception.getErrorCode());
            verify(cloverMissionService).pauseCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/missions/clover/{userMissionId}/complete")
    class CompleteMissionTests {

        @Test
        @DisplayName("성공 - STARTED 상태의 미션 완료")
        void completeMission_StartedStatus_Success() {

            // Given
            CloverMissionStatusResponseDto mockResponse = new CloverMissionStatusResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.completeCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willReturn(mockResponse);

            // When
            ResponseHandler<CloverMissionStatusResponseDto> response = 
                cloverMissionController.completeMission(TEST_USER_MISSION_ID, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverMissionService).completeCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - STARTED 상태가 아닌 미션 완료 시도 시 400 에러")
        void completeMission_AlreadyCompleted_ThrowsBadRequest() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.completeCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                .willThrow(new CustomException(ErrorCode.INVALID_MISSION_STATUS));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> 
                cloverMissionController.completeMission(TEST_USER_MISSION_ID, principalDetails));
            
            assertEquals(ErrorCode.INVALID_MISSION_STATUS, exception.getErrorCode());
            verify(cloverMissionService).completeCloverMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/missions/clover/refill")
    class RefillCloverMissionTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 미션 리필 요청")
        void refillCloverMission_AuthenticatedUser_Success() {

            // Given
            CloverMissionListResponseDto mockResponse = new CloverMissionListResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(cloverMissionService.assignCloverMissionList(TEST_MEMBER_ID)).willReturn(mockResponse);

            // When
            ResponseHandler<CloverMissionListResponseDto> response = 
                cloverMissionController.refillCloverMission(principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverMissionService).assignCloverMissionList(TEST_MEMBER_ID);
        }
    }
}