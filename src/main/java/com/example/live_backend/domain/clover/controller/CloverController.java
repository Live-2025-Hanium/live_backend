package com.example.live_backend.domain.clover.controller;

import com.example.live_backend.domain.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.clover.service.MissionService;
import com.example.live_backend.global.error.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions")
@Tag(name = "Clover", description = "클로버 미션 관련 API")
@Slf4j
public class CloverController {

    private final MissionService missionService;

    @GetMapping
    @Operation(summary = "클로버 미션 리스트 조회 ",
            description = "클로버 미션 리스트를 조회합니다.")
    public ResponseHandler<CloverMissionListResponseDto> getCloverMissionList() {

        log.info("클로버 미션 리스트(3개) 조회 API 호출");

        CloverMissionListResponseDto response = missionService.getCloverMissionList();

        log.info("클로버 미션 리스트 조회 완료 - 총 {}개 미션", response.getMissions().size());

        return ResponseHandler.response(response);
    }
}
