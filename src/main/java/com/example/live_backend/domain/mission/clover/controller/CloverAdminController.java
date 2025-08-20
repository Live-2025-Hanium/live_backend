package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.controller.docs.CloverAdminControllerDocs;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverAdminService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AdminApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/missions/clover")
@Slf4j
public class CloverAdminController implements CloverAdminControllerDocs {

    private final CloverAdminService cloverAdminService;

    @Override
    @PostMapping("/register")
    @AdminApi(reason = "클로버 미션 등록은 관리자만 가능합니다.")
    public ResponseHandler<AdminRegisterCloverMissionResponseDto> registerCloverMissions(
            @RequestBody AdminRegisterCloverMissionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        String role = userDetails.getRole();
        AdminRegisterCloverMissionResponseDto response = cloverAdminService.registerCloverMission(requestDto, role);

        return ResponseHandler.success(response);
    }

}
