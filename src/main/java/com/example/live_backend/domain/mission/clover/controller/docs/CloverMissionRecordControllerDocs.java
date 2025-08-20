package com.example.live_backend.domain.mission.clover.controller.docs;

import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordRequestDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Clover Mission Record", description = "클로버 미션 기록 관련 API")
public interface CloverMissionRecordControllerDocs {

    @Operation(summary = "클로버 미션 기록 추가", description = "완료된 클로버 미션에 대한 소감과 난이도, 인증샷(인증샷 미션인 경우)을 기록합니다.")
    ResponseHandler<CloverMissionRecordResponseDto> addMissionRecord(
            @RequestBody CloverMissionRecordRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 미션 기록 한개만 조회", description = "완료된 특정 클로버 미션 기록의 정보를 조회합니다.")
    ResponseHandler<CloverMissionRecordResponseDto> getMissionRecord(
            @Parameter(description = "사용자 미션 ID")
            @PathVariable Long userMissionId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "클로버 미션 기록 수정", description = "완료된 클로버 미션 기록의 소감, 난이도, 인증샷 정보를 수정합니다.")
    ResponseHandler<CloverMissionRecordResponseDto> updateMissionRecord(
            @RequestBody CloverMissionRecordRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );
}