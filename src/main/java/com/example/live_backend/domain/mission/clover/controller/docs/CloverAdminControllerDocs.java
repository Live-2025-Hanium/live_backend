package com.example.live_backend.domain.mission.clover.controller.docs;

import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Clover-Admin", description = "클로버 미션 관련 관리자 API")
public interface CloverAdminControllerDocs {

    @Operation(summary = "관리자 -클로버 미션 등록", description = "관리자 기능으로 클로버 미션을 등록합니다.")
    ResponseHandler<AdminRegisterCloverMissionResponseDto> registerCloverMissions(
            @RequestBody AdminRegisterCloverMissionRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );
}