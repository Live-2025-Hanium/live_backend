package com.example.live_backend.domain.mission.clover.controller;

import com.example.live_backend.domain.mission.clover.controller.docs.CloverMissionRecordControllerDocs;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordRequestDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.service.CloverMissionRecordService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions/records")
@Slf4j
public class CloverMissionRecordController implements CloverMissionRecordControllerDocs {

    private final CloverMissionRecordService cloverMissionRecordService;

    @Override
    @AuthenticatedApi(reason = "클로버 미션 기록 추가는 로그인한 사용자만 가능합니다")
    @PostMapping
    public ResponseHandler<CloverMissionRecordResponseDto> addMissionRecord(
            @RequestBody CloverMissionRecordRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        CloverMissionRecordResponseDto response = cloverMissionRecordService.addMissionRecord(memberId, requestDto);

        return ResponseHandler.success(response);
    }

    @Override
    @AuthenticatedApi(reason = "클로버 미션 기록 한개 조회는 로그인한 사용자만 가능합니다")
    @GetMapping("/{userMissionId}")
    public ResponseHandler<CloverMissionRecordResponseDto> getMissionRecord(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        CloverMissionRecordResponseDto response = cloverMissionRecordService.getMissionRecord(memberId, userMissionId);

        return ResponseHandler.success(response);
    }

    @Override
    @AuthenticatedApi(reason = "클로버 미션 기록 수정은 로그인한 사용자만 가능합니다")
    @PutMapping
    public ResponseHandler<CloverMissionRecordResponseDto> updateMissionRecord(
            @RequestBody CloverMissionRecordRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        CloverMissionRecordResponseDto response = cloverMissionRecordService.updateMissionRecord(memberId, requestDto);

        return ResponseHandler.success(response);
    }
}