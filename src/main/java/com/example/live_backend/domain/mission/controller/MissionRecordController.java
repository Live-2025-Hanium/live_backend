package com.example.live_backend.domain.mission.controller;

import com.example.live_backend.domain.mission.dto.MissionRecordRequestDto;
import com.example.live_backend.domain.mission.dto.MissionRecordResponseDto;
import com.example.live_backend.domain.mission.service.MissionRecordService;
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
@RequestMapping("/api/v1/missions/records")
@Tag(name = "Mission Record", description = "미션 기록 관련 API")
@Slf4j
public class MissionRecordController {

    private final MissionRecordService missionRecordService;

    @PostMapping
    @Operation(summary = "클로버 미션 기록 추가", description = "완료된 클로버 미션에 대한 소감과 난이도, 인증샷(인증샷 미션인 경우)을 기록합니다.")
    public ResponseHandler<MissionRecordResponseDto> addMissionRecord(
            @RequestBody MissionRecordRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        MissionRecordResponseDto response = missionRecordService.addMissionRecord(memberId, requestDto);

        return ResponseHandler.success(response);
    }

    @GetMapping("/{userMissionId}")
    @Operation(summary = "클로버 미션 기록 한개만 조회", description = "완료된 특정 클로버 미션 기록의 정보를 조회합니다.")
    public ResponseHandler<MissionRecordResponseDto> getMissionRecord(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        MissionRecordResponseDto response = missionRecordService.getMissionRecord(memberId, userMissionId);

        return ResponseHandler.success(response);
    }

    @PutMapping
    @Operation(summary = "클로버 미션 기록 수정", description = "완료된 클로버 미션 기록의 소감, 난이도, 인증샷 정보를 수정합니다.")
    public ResponseHandler<MissionRecordResponseDto> updateMissionRecord(
            @RequestBody MissionRecordRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        MissionRecordResponseDto response = missionRecordService.updateMissionRecord(memberId, requestDto);

        return ResponseHandler.success(response);
    }
}