package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.Enum.TargetUserType;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverAdminService;
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
@DisplayName("CloverAdminController 테스트")
class CloverAdminControllerTest {

    @Mock
    private CloverAdminService cloverAdminService;

    @Mock
    private PrincipalDetails principalDetails;

    @InjectMocks
    private CloverAdminController cloverAdminController;

    @Nested
    @DisplayName("POST /api/admin/v1/missions/clover/register")
    class RegisterCloverMissionTests {

        @Test
        @DisplayName("성공 - 관리자에 의한 클로버 미션 등록")
        void registerCloverMission_Admin_Success() {

            // Given
            AdminRegisterCloverMissionRequestDto requestDto = mock(AdminRegisterCloverMissionRequestDto.class);

            AdminRegisterCloverMissionResponseDto mockResponse = AdminRegisterCloverMissionResponseDto.builder()
                    .cloverMissionId(10L)
                    .missionTitle("걷기 미션")
                    .description("하루 10분 걷기")
                    .missionCategory(MissionCategory.HEALTH)
                    .missionDifficulty(MissionDifficulty.EASY)
                    .targetUserType(TargetUserType.HEALTH_VULNERABLE)
                    .vectorDocument("미션 제목: 걷기 미션, 미션 설명: ..., 도움을 줄 수 있는 사용자의 특성: ..., 기대 효과: ...")
                    .build();

            given(cloverAdminService.registerCloverMission(eq(requestDto))).willReturn(mockResponse);

            // When
            ResponseHandler<AdminRegisterCloverMissionResponseDto> response =
                    cloverAdminController.registerCloverMissions(requestDto, principalDetails);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(mockResponse, response.getData());
            verify(cloverAdminService).registerCloverMission(requestDto);
        }
    }
}