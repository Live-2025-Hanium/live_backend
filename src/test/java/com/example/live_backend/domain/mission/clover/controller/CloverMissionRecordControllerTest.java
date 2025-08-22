package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordRequestDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverMissionRecordService;
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
@DisplayName("CloverMissionRecordController 테스트")
class CloverMissionRecordControllerTest {

	@Mock
	private CloverMissionRecordService cloverMissionRecordService;

	@Mock
	private PrincipalDetails principalDetails;

	@InjectMocks
	private CloverMissionRecordController cloverMissionRecordController;

	private final Long TEST_MEMBER_ID = 1L;
	private final Long TEST_USER_MISSION_ID = 100L;

	@Nested
	@DisplayName("POST /api/v1/missions/records")
	class AddMissionRecordTests {

		@Test
		@DisplayName("성공 - 미션 기록 추가")
		void addMissionRecord_Success() {

			// Given
			CloverMissionRecordRequestDto requestDto = mock(CloverMissionRecordRequestDto.class);
			CloverMissionRecordResponseDto mockResponse = new CloverMissionRecordResponseDto();
			given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
			given(cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, requestDto)).willReturn(mockResponse);

			// When
			ResponseHandler<CloverMissionRecordResponseDto> response =
					cloverMissionRecordController.addMissionRecord(requestDto, principalDetails);

			// Then
			assertTrue(response.isSuccess());
			assertEquals(mockResponse, response.getData());
			verify(cloverMissionRecordService).addMissionRecord(TEST_MEMBER_ID, requestDto);
		}
	}

	@Nested
	@DisplayName("GET /api/v1/missions/records/{userMissionId}")
	class GetMissionRecordTests {

		@Test
		@DisplayName("성공 - 미션 기록 1개 조회")
		void getMissionRecord_Success() {

			// Given
			CloverMissionRecordResponseDto mockResponse = new CloverMissionRecordResponseDto();
			given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
			given(cloverMissionRecordService.getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID))
					.willReturn(mockResponse);

			// When
			ResponseHandler<CloverMissionRecordResponseDto> response =
					cloverMissionRecordController.getMissionRecord(TEST_USER_MISSION_ID, principalDetails);

			// Then
			assertTrue(response.isSuccess());
			assertEquals(mockResponse, response.getData());
			verify(cloverMissionRecordService).getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 미션 기록 조회 시 404")
		void getMissionRecord_NotFound() {

			// Given
			Long nonExistentId = 999L;
			given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
			given(cloverMissionRecordService.getMissionRecord(TEST_MEMBER_ID, nonExistentId))
					.willThrow(new CustomException(ErrorCode.MISSION_NOT_FOUND));

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () ->
					cloverMissionRecordController.getMissionRecord(nonExistentId, principalDetails));

			assertEquals(ErrorCode.MISSION_NOT_FOUND, exception.getErrorCode());
			verify(cloverMissionRecordService).getMissionRecord(TEST_MEMBER_ID, nonExistentId);
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 미션 기록 조회 시 403")
		void getMissionRecord_Forbidden() {

			// Given
			given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
			given(cloverMissionRecordService.getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID))
					.willThrow(new CustomException(ErrorCode.MISSION_FORBIDDEN));

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () ->
					cloverMissionRecordController.getMissionRecord(TEST_USER_MISSION_ID, principalDetails));

			assertEquals(ErrorCode.MISSION_FORBIDDEN, exception.getErrorCode());
			verify(cloverMissionRecordService).getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID);
		}
	}

	@Nested
	@DisplayName("PUT /api/v1/missions/records")
	class UpdateMissionRecordTests {

		@Test
		@DisplayName("성공 - 미션 기록 수정")
		void updateMissionRecord_Success() {

			// Given
			CloverMissionRecordRequestDto requestDto = mock(CloverMissionRecordRequestDto.class);
			CloverMissionRecordResponseDto mockResponse = new CloverMissionRecordResponseDto();
			given(principalDetails.getMemberId()).willReturn(TEST_MEMBER_ID);
			given(cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, requestDto))
					.willReturn(mockResponse);

			// When
			ResponseHandler<CloverMissionRecordResponseDto> response =
					cloverMissionRecordController.updateMissionRecord(requestDto, principalDetails);

			// Then
			assertTrue(response.isSuccess());
			assertEquals(mockResponse, response.getData());
			verify(cloverMissionRecordService).updateMissionRecord(TEST_MEMBER_ID, requestDto);
		}
	}
}


