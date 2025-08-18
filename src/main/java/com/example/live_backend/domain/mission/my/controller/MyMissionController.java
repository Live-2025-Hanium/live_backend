package com.example.live_backend.domain.mission.my.controller;

import com.example.live_backend.domain.mission.my.dto.MyMissionRecordResponseDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionResponseDto;
import com.example.live_backend.domain.mission.my.service.MyMissionService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions/my")
@Tag(name = "My Mission", description = "마이 미션 관련 API")
@Slf4j
public class MyMissionController {

    private final MyMissionService myMissionService;

    @PostMapping
    @Operation(summary = "마이 미션 생성", description = "마이 미션을 생성합니다.")
    public ResponseHandler<MyMissionResponseDto> createMyMission(
            @RequestBody MyMissionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionResponseDto response = myMissionService.createMyMission(requestDto, memberId);

        return ResponseHandler.success(response);
    }

    @PutMapping("/{myMissionId}")
    @Operation(summary = "마이 미션 수정", description = "마이 미션을 수정합니다.")
    public ResponseHandler<MyMissionResponseDto> updateMyMission(
            @PathVariable Long myMissionId,
            @RequestBody MyMissionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionResponseDto response = myMissionService.updateMyMission(myMissionId, requestDto, memberId);

        return ResponseHandler.success(response);
    }

    @DeleteMapping("/{myMissionId}")
    @Operation(summary = "마이 미션 삭제", description = "마이 미션을 삭제합니다.")
    public ResponseHandler<Void> deleteMyMission(
            @PathVariable Long myMissionId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        myMissionService.deleteMyMission(myMissionId, memberId);

        return ResponseHandler.success(null);
    }

    @GetMapping
    @Operation(summary = "마이 미션 전체 리스트 조회", description = "마이 미션 전체 리스트를 조회합니다.")
    public ResponseHandler<List<MyMissionResponseDto>> getMyMissionsList(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        List<MyMissionResponseDto> response = myMissionService.getMyMissionsList(memberId);

        return ResponseHandler.success(response);
    }

    @GetMapping("/today")
    @Operation(summary = "금일 수행해야 하는 마이 미션 리스트 조회", description = "금일 수행할 마이 미션의 전체 리스트를 조회합니다.")
    public ResponseHandler<List<MyMissionRecordResponseDto>> getTodayMyMissionsList(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();

        List<MyMissionRecordResponseDto> response = myMissionService.getTodayMissions(memberId);
        return ResponseHandler.success(response);
    }

    @PatchMapping("/{userMissionId}/complete")
    @Operation(summary = "마이 미션 수행 완료", description = "금일 수행한 마이 미션의 상태를 완료 처리합니다.")
    public ResponseHandler<MyMissionRecordResponseDto> completeMyMission(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionRecordResponseDto response = myMissionService.completeMyMission(userMissionId, memberId);

        return ResponseHandler.success(response);
    }
}
