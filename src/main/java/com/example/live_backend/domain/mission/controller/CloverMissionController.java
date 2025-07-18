package com.example.live_backend.domain.mission.controller;

import com.example.live_backend.domain.mission.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.mission.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.mission.service.CloverMissionService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions/clover")
@Tag(name = "Clover", description = "클로버 미션 관련 API")
@Slf4j
public class CloverMissionController {

	private final CloverMissionService cloverMissionService;

	@GetMapping
	@Operation(summary = "클로버 미션 리스트 조회 ",
		description = "클로버 미션 리스트를 조회합니다.")
	public ResponseHandler<CloverMissionListResponseDto> getCloverMissionList() {

		try {
			log.info("클로버 미션 리스트(3개) 조회 API 호출");
			CloverMissionListResponseDto response = cloverMissionService.getCloverMissionList();

			log.info("클로버 미션 리스트 조회 완료 - 총 {}개 미션", response.getMissions().size());
			return ResponseHandler.success(response);
		} catch (Exception e) {
			return ResponseHandler.error(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/{userMissionId}")
	@Operation(summary = "클로버 미션(1개) 상세 조회 ",
		description = "클로버 미션 1개의 상세 정보를 조회합니다.")
	public ResponseHandler<CloverMissionResponseDto> getCloverMissionInfo(@PathVariable Long userMissionId) {

		try {
			log.info("클로버 미션 상세 조회 API 호출");

			CloverMissionResponseDto response = cloverMissionService.getCloverMissionInfo(userMissionId);

			return ResponseHandler.success(response);
		} catch (Exception e) {
			return ResponseHandler.error(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@PatchMapping("/{userMissionId}/start")
	@Operation(summary = "클로버 미션 시작(Started)", description = "클로버 미션을 시작 상태로 변경합니다.")
	public ResponseHandler<?> startMission(@PathVariable Long userMissionId) {

		log.info("클로버 미션 시작 API 호출 - userMissionId: {}", userMissionId);

		cloverMissionService.startCloverMission(userMissionId);

		return ResponseHandler.success(null);

	}

	@PatchMapping("/{userMissionId}/pause")
	@Operation(summary = "클로버 미션 일시정지(Paused)", description = "클로버 미션을 일시정지 상태로 변경합니다.")
	public ResponseHandler<?> pauseMission(@PathVariable Long userMissionId) {

		log.info("클로버 미션 일시정지 API 호출 - userMissionId: {}", userMissionId);

		cloverMissionService.pauseCloverMission(userMissionId);

		return ResponseHandler.success(null);
	}

	@PatchMapping("/{userMissionId}/complete")
	@Operation(summary = "클로버 미션 완료(Completed)", description = "클로버 미션을 완료 상태로 변경합니다.")
	public ResponseHandler<?> completeMission(@PathVariable Long userMissionId) {

		log.info("클로버 미션 완료 API 호출 - userMissionId: {}", userMissionId);

		cloverMissionService.completeCloverMission(userMissionId);

		return ResponseHandler.success(null);
	}
}
