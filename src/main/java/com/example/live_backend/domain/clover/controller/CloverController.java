package com.example.live_backend.domain.clover.controller;

import com.example.live_backend.domain.clover.controller.docs.CloverControllerDocs;
import com.example.live_backend.domain.clover.dto.CloverResponseDto;
import com.example.live_backend.domain.clover.service.CloverService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clover")
public class CloverController implements CloverControllerDocs {

    private final CloverService cloverService;

    @Override
    @AuthenticatedApi(reason = "클로버 개수 조회는 로그인한 사용자만 가능합니다")
    @GetMapping
    public ResponseHandler<CloverResponseDto> getCloverCount(
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        Long memberId = userDetails.getMemberId();

        CloverResponseDto responseDto = cloverService.getCloverCount(memberId);
        return ResponseHandler.success(responseDto);
    }
}
