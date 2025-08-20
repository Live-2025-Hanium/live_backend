package com.example.live_backend.domain.mission.my.controller;

import com.example.live_backend.domain.mission.my.controller.docs.MyMissionControllerDocs;
import com.example.live_backend.domain.mission.my.dto.MyMissionRecordResponseDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionResponseDto;
import com.example.live_backend.domain.mission.my.service.MyMissionService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions/my")
@Slf4j
public class MyMissionController implements MyMissionControllerDocs {

    private final MyMissionService myMissionService;

    @Override
    @PostMapping
    @AuthenticatedApi(reason = "마이 미션 생성은 로그인한 사용자만 가능합니다")
    public ResponseHandler<MyMissionResponseDto> createMyMission(
            @RequestBody MyMissionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionResponseDto response = myMissionService.createMyMission(requestDto, memberId);

        return ResponseHandler.success(response);
    }

    @Override
    @PutMapping("/{myMissionId}")
    @AuthenticatedApi(reason = "마이 미션 수정은 로그인한 사용자만 가능합니다")
    public ResponseHandler<MyMissionResponseDto> updateMyMission(
            @PathVariable Long myMissionId,
            @RequestBody MyMissionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionResponseDto response = myMissionService.updateMyMission(myMissionId, requestDto, memberId);

        return ResponseHandler.success(response);
    }

    @Override
    @DeleteMapping("/{myMissionId}")
    @AuthenticatedApi(reason = "마이 미션 삭제는 로그인한 사용자만 가능합니다")
    public ResponseHandler<Void> deleteMyMission(
            @PathVariable Long myMissionId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        myMissionService.deleteMyMission(myMissionId, memberId);

        return ResponseHandler.success(null);
    }

    @Override
    @GetMapping
    @AuthenticatedApi(reason = "마이 미션 전체 리스트 조회는 로그인한 사용자만 가능합니다")
    public ResponseHandler<List<MyMissionResponseDto>> getMyMissionsList(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        List<MyMissionResponseDto> response = myMissionService.getMyMissionsList(memberId);

        return ResponseHandler.success(response);
    }

    @Override
    @GetMapping("/today")
    @AuthenticatedApi(reason = "금일 수행해야 하는 마이 미션 리스트 조회는 로그인한 사용자만 가능합니다")
    public ResponseHandler<List<MyMissionRecordResponseDto>> getTodayMyMissionsList(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();

        List<MyMissionRecordResponseDto> response = myMissionService.getTodayMissions(memberId);
        return ResponseHandler.success(response);
    }

    @Override
    @PatchMapping("/{userMissionId}/complete")
    @AuthenticatedApi(reason = "마이 미션 수행 완료 처리는 로그인한 사용자만 가능합니다")
    public ResponseHandler<MyMissionRecordResponseDto> completeMyMission(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionRecordResponseDto response = myMissionService.completeMyMission(userMissionId, memberId);

        return ResponseHandler.success(response);
    }

    @Override
    @PatchMapping("/{myMissionId}/active")
    @AuthenticatedApi(reason = "마이 미션 활성/바활성화는 로그인한 사용자만 가능합니다")
    public ResponseHandler<MyMissionResponseDto> changeActive(
            @PathVariable Long myMissionId,
            @RequestParam boolean active,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {

        Long memberId = userDetails.getMemberId();
        MyMissionResponseDto response = myMissionService.changeActive(memberId, myMissionId, active);

        return ResponseHandler.success(response);
    }
}
