package com.example.live_backend.domain.mission.my.controller.docs;

import com.example.live_backend.domain.mission.my.dto.MyMissionRecordResponseDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "My Mission", description = "마이 미션 관련 API")
public interface MyMissionControllerDocs {

    @Operation(summary = "마이 미션 생성", description = "마이 미션을 생성합니다.")
    ResponseHandler<MyMissionResponseDto> createMyMission(
            @RequestBody MyMissionRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이 미션 수정", description = "마이 미션을 수정합니다.")
    ResponseHandler<MyMissionResponseDto> updateMyMission(
            @Parameter(description = "마이 미션 ID") @PathVariable Long myMissionId,
            @RequestBody MyMissionRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이 미션 삭제", description = "마이 미션을 삭제합니다.")
    ResponseHandler<Void> deleteMyMission(
            @Parameter(description = "마이 미션 ID") @PathVariable Long myMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이 미션 전체 리스트 조회", description = "마이 미션 전체 리스트를 조회합니다.")
    ResponseHandler<List<MyMissionResponseDto>> getMyMissionsList(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "금일 수행해야 하는 마이 미션 리스트 조회", description = "금일 수행할 마이 미션의 전체 리스트를 조회합니다.")
    ResponseHandler<List<MyMissionRecordResponseDto>> getTodayMyMissionsList(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이 미션 수행 완료", description = "금일 수행한 마이 미션의 상태를 완료 처리합니다.")
    ResponseHandler<MyMissionRecordResponseDto> completeMyMission(
            @Parameter(description = "사용자에게 할당된 미션 ID") @PathVariable Long userMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "마이 미션 활성/비활성", description = "마이 미션의 토글을 On/Off 설정합니다. (isActive 값을 true/false로 변경)")
    ResponseHandler<MyMissionResponseDto> changeActive(
            @Parameter(description = "마이 미션 ID") @PathVariable Long myMissionId,
            @RequestParam boolean active,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );
}