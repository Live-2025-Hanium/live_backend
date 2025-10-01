package com.example.live_backend.domain.clover.controller.docs;

import com.example.live_backend.domain.clover.dto.CloverResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "클로버", description = "클로버 관련 API")
public interface CloverControllerDocs {

    @Operation(summary = "클로버 개수 조회", description = "현재 사용자의 클로버 개수를 조회합니다.")
    ResponseHandler<CloverResponseDto> getCloverCount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails userDetails
    );
}
