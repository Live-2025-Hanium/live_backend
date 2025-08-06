package com.example.live_backend.domain.mission.controller;

import com.example.live_backend.domain.mission.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.service.CloverAdminService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/missions/clover")
@Tag(name = "Clover-Admin", description = "클로버 미션 관련 관리자 API")
@Slf4j
public class CloverAdminController {

    private final CloverAdminService cloverAdminService;

    @PostMapping("/register")
    @Operation(summary = "관리자 -클로버 미션 등록", description = "관리자 기능으로 클로버 미션을 등록합니다.")
    public ResponseHandler<AdminRegisterCloverMissionResponseDto> registerCloverMissions(
            @RequestBody AdminRegisterCloverMissionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        String role = userDetails.getRole();
        AdminRegisterCloverMissionResponseDto response = cloverAdminService.registerCloverMission(requestDto, role);

        return ResponseHandler.success(response);
    }

}
