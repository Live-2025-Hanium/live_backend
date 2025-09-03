package com.example.live_backend.domain.mission.my.controller;

import com.example.live_backend.domain.mission.my.dto.MyMissionRecordResponseDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionResponseDto;
import com.example.live_backend.domain.mission.my.service.MyMissionService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MyMissionController 테스트")
class MyMissionControllerTest {

    @Mock
    private MyMissionService myMissionService;

    @Mock
    private PrincipalDetails principalDetails;

    @InjectMocks
    private MyMissionController myMissionController;

    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_MY_MISSION_ID = 100L;
    private final Long TEST_USER_MISSION_ID = 200L;

    @Nested
    @DisplayName("POST /api/v1/missions/my")
    class CreateMyMissionTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 생성")
        void createMyMission_AuthenticatedUser_Success() {

            // Given
            MyMissionRequestDto requestDto = new MyMissionRequestDto();
            MyMissionResponseDto mockResponse = new MyMissionResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.createMyMission(requestDto, TEST_MEMBER_ID)).willReturn(mockResponse);

            // When
            ResponseHandler<MyMissionResponseDto> response =
                    myMissionController.createMyMission(requestDto, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).createMyMission(requestDto, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자의 마이미션 생성 시도")
        void createMyMission_NonExistentUser_ThrowsUserNotFound() {

            // Given
            MyMissionRequestDto requestDto = new MyMissionRequestDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.createMyMission(requestDto, TEST_MEMBER_ID))
                    .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.createMyMission(requestDto, principalDetails));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(myMissionService).createMyMission(requestDto, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/missions/my/{myMissionId}")
    class UpdateMyMissionTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 수정")
        void updateMyMission_AuthenticatedUser_Success() {

            // Given
            MyMissionRequestDto requestDto = new MyMissionRequestDto();
            MyMissionResponseDto mockResponse = new MyMissionResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID))
                    .willReturn(mockResponse);

            // When
            ResponseHandler<MyMissionResponseDto> response =
                    myMissionController.updateMyMission(TEST_MY_MISSION_ID, requestDto, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 미션 수정 시도")
        void updateMyMission_NonExistentMission_ThrowsMissionNotFound() {

            // Given
            MyMissionRequestDto requestDto = new MyMissionRequestDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID))
                    .willThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.updateMyMission(TEST_MY_MISSION_ID, requestDto, principalDetails));

            assertEquals(ErrorCode.MISSION_NOT_FOUND, exception.getErrorCode());
            verify(myMissionService).updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 권한이 없는 사용자의 미션 수정 시도")
        void updateMyMission_UnauthorizedUser_ThrowsUpdateDenied() {

            // Given
            MyMissionRequestDto requestDto = new MyMissionRequestDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID))
                    .willThrow(new CustomException(ErrorCode.MISSION_UPDATE_DENIED));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.updateMyMission(TEST_MY_MISSION_ID, requestDto, principalDetails));

            assertEquals(ErrorCode.MISSION_UPDATE_DENIED, exception.getErrorCode());
            verify(myMissionService).updateMyMission(TEST_MY_MISSION_ID, requestDto, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/missions/my/{myMissionId}")
    class DeleteMyMissionTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 삭제")
        void deleteMyMission_AuthenticatedUser_Success() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            doNothing().when(myMissionService).deleteMyMission(TEST_MY_MISSION_ID, TEST_MEMBER_ID);

            // When
            ResponseHandler<Void> response =
                    myMissionController.deleteMyMission(TEST_MY_MISSION_ID, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertNull(response.getData());
            verify(myMissionService).deleteMyMission(TEST_MY_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 권한이 없는 사용자의 미션 삭제 시도")
        void deleteMyMission_UnauthorizedUser_ThrowsDeleteDenied() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            doThrow(new CustomException(ErrorCode.MISSION_DELETE_DENIED))
                    .when(myMissionService).deleteMyMission(TEST_MY_MISSION_ID, TEST_MEMBER_ID);

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.deleteMyMission(TEST_MY_MISSION_ID, principalDetails));

            assertEquals(ErrorCode.MISSION_DELETE_DENIED, exception.getErrorCode());
            verify(myMissionService).deleteMyMission(TEST_MY_MISSION_ID, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/missions/my")
    class GetMyMissionsListTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 목록 조회")
        void getMyMissionsList_AuthenticatedUser_Success() {

            // Given
            List<MyMissionResponseDto> mockResponse = List.of(new MyMissionResponseDto());
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.getMyMissionsList(TEST_MEMBER_ID)).willReturn(mockResponse);

            // When
            ResponseHandler<List<MyMissionResponseDto>> response =
                    myMissionController.getMyMissionsList(principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).getMyMissionsList(TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/missions/my/today")
    class GetTodayMyMissionsListTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 오늘 마이미션 목록 조회")
        void getTodayMyMissionsList_AuthenticatedUser_Success() {

            // Given
            List<MyMissionRecordResponseDto> mockResponse = List.of(new MyMissionRecordResponseDto());
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.getTodayMissions(TEST_MEMBER_ID)).willReturn(mockResponse);

            // When
            ResponseHandler<List<MyMissionRecordResponseDto>> response =
                    myMissionController.getTodayMyMissionsList(principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).getTodayMissions(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자의 오늘 미션 조회")
        void getTodayMyMissionsList_NonExistentUser_ThrowsUserNotFound() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.getTodayMissions(TEST_MEMBER_ID))
                    .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.getTodayMyMissionsList(principalDetails));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(myMissionService).getTodayMissions(TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/missions/my/{userMissionId}/complete")
    class CompleteMyMissionTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 완료 처리")
        void completeMyMission_AuthenticatedUser_Success() {

            // Given
            MyMissionRecordResponseDto mockResponse = new MyMissionRecordResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                    .willReturn(mockResponse);

            // When
            ResponseHandler<MyMissionRecordResponseDto> response =
                    myMissionController.completeMyMission(TEST_USER_MISSION_ID, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 만료된 미션 완료 시도")
        void completeMyMission_ExpiredMission_ThrowsMissionExpired() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                    .willThrow(new CustomException(ErrorCode.MISSION_EXPIRED));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.completeMyMission(TEST_USER_MISSION_ID, principalDetails));

            assertEquals(ErrorCode.MISSION_EXPIRED, exception.getErrorCode());
            verify(myMissionService).completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 권한이 없는 사용자의 미션 완료 시도")
        void completeMyMission_UnauthorizedUser_ThrowsMissionForbidden() {

            // Given
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID))
                    .willThrow(new CustomException(ErrorCode.MISSION_FORBIDDEN));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.completeMyMission(TEST_USER_MISSION_ID, principalDetails));

            assertEquals(ErrorCode.MISSION_FORBIDDEN, exception.getErrorCode());
            verify(myMissionService).completeMyMission(TEST_USER_MISSION_ID, TEST_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/missions/my/{myMissionId}/active")
    class ChangeActiveTests {

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 활성화")
        void changeActive_AuthenticatedUser_ActivateSuccess() {

            // Given
            boolean active = true;
            MyMissionResponseDto mockResponse = new MyMissionResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active))
                    .willReturn(mockResponse);

            // When
            ResponseHandler<MyMissionResponseDto> response =
                    myMissionController.changeActive(TEST_MY_MISSION_ID, active, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active);
        }

        @Test
        @DisplayName("성공 - 인증된 사용자의 마이미션 비활성화")
        void changeActive_AuthenticatedUser_DeactivateSuccess() {

            // Given
            boolean active = false;
            MyMissionResponseDto mockResponse = new MyMissionResponseDto();
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active))
                    .willReturn(mockResponse);

            // When
            ResponseHandler<MyMissionResponseDto> response =
                    myMissionController.changeActive(TEST_MY_MISSION_ID, active, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(myMissionService).changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active);
        }

        @Test
        @DisplayName("실패 - 권한이 없는 사용자의 미션 활성화 상태 변경 시도")
        void changeActive_UnauthorizedUser_ThrowsUpdateDenied() {

            // Given
            boolean active = true;
            given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
            given(myMissionService.changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active))
                    .willThrow(new CustomException(ErrorCode.MISSION_UPDATE_DENIED));

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () ->
                    myMissionController.changeActive(TEST_MY_MISSION_ID, active, principalDetails));

            assertEquals(ErrorCode.MISSION_UPDATE_DENIED, exception.getErrorCode());
            verify(myMissionService).changeActive(TEST_MEMBER_ID, TEST_MY_MISSION_ID, active);
        }
    }
}