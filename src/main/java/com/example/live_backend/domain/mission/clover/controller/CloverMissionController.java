package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.controller.docs.CloverMissionControllerDocs;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionStatusResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverMissionService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions/clover")
@Slf4j
public class CloverMissionController implements CloverMissionControllerDocs {

	private final CloverMissionService cloverMissionService;

	@Override
	@GetMapping
	@AuthenticatedApi(reason = "클로버 미션 리스트 조회는 로그인한 사용자만 가능합니다")
	public ResponseHandler<CloverMissionListResponseDto> getCloverMissionList(
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionListResponseDto response = cloverMissionService.getCloverMissionList(userId);

		return ResponseHandler.success(response);
	}

	@Override
	@GetMapping("/{userMissionId}")
	@AuthenticatedApi(reason = "클로버 미션 상세 조회는 로그인한 사용자만 가능합니다")
	public ResponseHandler<CloverMissionResponseDto> getCloverMissionInfo(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionResponseDto response = cloverMissionService.getCloverMissionInfo(userMissionId, userId);

		return ResponseHandler.success(response);
	}

	@Override
	@PatchMapping("/{userMissionId}/start")
	@AuthenticatedApi(reason = "클로버 미션 상태 변경은 로그인한 사용자만 가능합니다")
	public ResponseHandler<CloverMissionStatusResponseDto> startMission(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionStatusResponseDto response = cloverMissionService.startCloverMission(userMissionId, userId);

		return ResponseHandler.success(response);

	}

	@Override
	@PatchMapping("/{userMissionId}/pause")
	@AuthenticatedApi(reason = "클로버 미션 상태 변경은 로그인한 사용자만 가능합니다")
	public ResponseHandler<CloverMissionStatusResponseDto> pauseMission(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionStatusResponseDto response = cloverMissionService.pauseCloverMission(userMissionId, userId);

		return ResponseHandler.success(response);
	}

	@Override
	@PatchMapping("/{userMissionId}/complete")
	@AuthenticatedApi(reason = "클로버 미션 상태 변경은 로그인한 사용자만 가능합니다")
	public ResponseHandler<CloverMissionStatusResponseDto> completeMission(
			@PathVariable Long userMissionId,
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionStatusResponseDto response = cloverMissionService.completeCloverMission(userMissionId, userId);

		return ResponseHandler.success(response);
	}

	@Override
	@GetMapping("/refill")
	@AuthenticatedApi(reason = "클로버 미션 리필은 로그인한 사용자만 가능합니다")
	public ResponseHandler<CloverMissionListResponseDto> refillCloverMission(
			@AuthenticationPrincipal PrincipalDetails userDetails) {

		Long userId = userDetails.getMemberId();
		CloverMissionListResponseDto response = cloverMissionService.assignCloverMissionList(userId);

		return ResponseHandler.success(response);
	}
}
