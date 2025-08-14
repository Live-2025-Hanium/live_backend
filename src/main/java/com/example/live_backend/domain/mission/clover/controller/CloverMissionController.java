package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverMissionService;
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
@RequestMapping("/api/v1/missions/clover")
@Tag(name = "Clover Mission", description = "클로버 미션 관련 API")
@Slf4j
public class CloverMissionController {

	private final CloverMissionService cloverMissionService;

	@GetMapping
	@Operation(summary = "클로버 미션 리스트 조회 ", description = "클로버 미션 리스트를 조회합니다.")
	public ResponseHandler<CloverMissionListResponseDto> getCloverMissionList(
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionListResponseDto response = cloverMissionService.getCloverMissionList(userId);

		return ResponseHandler.success(response);
	}

	@GetMapping("/{userMissionId}")
	@Operation(summary = "클로버 미션(1개) 상세 조회 ",
			description = "클로버 미션 1개의 상세 정보를 조회합니다.")
	public ResponseHandler<CloverMissionRecordResponseDto> getCloverMissionInfo(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionRecordResponseDto response = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

		return ResponseHandler.success(response);
	}

	@PatchMapping("/{userMissionId}/start")
	@Operation(summary = "클로버 미션 시작(Started)", description = "클로버 미션을 시작 상태로 변경합니다.")
	public ResponseHandler<CloverMissionStatusResponseDto> startMission(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionStatusResponseDto response = cloverMissionService.startCloverMission(userMissionId, userId);

		return ResponseHandler.success(response);

	}

	@PatchMapping("/{userMissionId}/pause")
	@Operation(summary = "클로버 미션 일시정지(Paused)", description = "클로버 미션을 일시정지 상태로 변경합니다.")
	public ResponseHandler<CloverMissionStatusResponseDto> pauseMission(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionStatusResponseDto response = cloverMissionService.pauseCloverMission(userMissionId, userId);

		return ResponseHandler.success(response);
	}

	@PatchMapping("/{userMissionId}/complete")
	@Operation(summary = "클로버 미션 완료(Completed)", description = "클로버 미션을 완료 상태로 변경합니다.")
	public ResponseHandler<CloverMissionStatusResponseDto> completeMission(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionStatusResponseDto response = cloverMissionService.completeCloverMission(userMissionId, userId);

		return ResponseHandler.success(response);
	}

	@GetMapping("/refill")
	@Operation(summary = "클로버 리필", description = "클로버 미션을 새롭게 할당합니다.")
	public ResponseHandler<CloverMissionListResponseDto> refillCloverMission(
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionListResponseDto response = cloverMissionService.assignCloverMissionList(userId);

		return ResponseHandler.success(response);
	}
}
