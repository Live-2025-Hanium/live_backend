package com.example.live_backend.domain.mission.clover.controller.docs;

import com.example.live_backend.domain.mission.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Clover Mission", description = "클로버 미션 관련 API")
public interface CloverMissionControllerDocs {

    @Operation(summary = "클로버 미션 리스트 조회 ", description = "클로버 미션 리스트를 조회합니다.")
    ResponseHandler<CloverMissionListResponseDto> getCloverMissionList(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 미션(1개) 상세 조회 ", description = "클로버 미션 1개의 상세 정보를 조회합니다.")
    ResponseHandler<CloverMissionResponseDto> getCloverMissionInfo(
            @Parameter(description = "사용자 미션 ID") @PathVariable Long userMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 미션 시작(Started)", description = "클로버 미션을 시작 상태로 변경합니다.")
    ResponseHandler<CloverMissionStatusResponseDto> startMission(
            @Parameter(description = "사용자 미션 ID") @PathVariable Long userMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 미션 일시정지(Paused)", description = "클로버 미션을 일시정지 상태로 변경합니다.")
    ResponseHandler<CloverMissionStatusResponseDto> pauseMission(
            @Parameter(description = "사용자 미션 ID") @PathVariable Long userMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 미션 완료(Completed)", description = "클로버 미션을 완료 상태로 변경합니다.")
    ResponseHandler<CloverMissionStatusResponseDto> completeMission(
            @Parameter(description = "사용자 미션 ID") @PathVariable Long userMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 리필", description = "클로버 미션을 새롭게 할당합니다.")
    ResponseHandler<CloverMissionListResponseDto> refillCloverMission(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );
}