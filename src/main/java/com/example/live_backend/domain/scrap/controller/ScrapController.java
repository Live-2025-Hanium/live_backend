package com.example.live_backend.domain.scrap.controller;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapCursorRequestDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapToggleResponseDto;
import com.example.live_backend.domain.scrap.service.ScrapService;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "스크랩", description = "스크랩 관련 API")
@RestController
@RequestMapping("/api/v1/scraps")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(summary = "게시글 스크랩 토글", description = "게시글 스크랩을 추가하거나 취소합니다")
    @AuthenticatedApi(reason = "스크랩은 로그인한 사용자만 가능합니다")
    @PostMapping("/boards/{boardId}/toggle")
    public ResponseHandler<ScrapToggleResponseDto> toggleScrap(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long boardId) {
        boolean isScraped = scrapService.toggleScrap(principalDetails.getMemberId(), boardId);
        return ResponseHandler.success(ScrapToggleResponseDto.of(isScraped));
    }

    @Operation(summary = "게시글 다중 스크랩 취소", description = "여러 게시글의 스크랩을 한번에 취소합니다")
    @AuthenticatedApi(reason = "스크랩 취소는 로그인한 사용자만 가능합니다")
    @DeleteMapping("/boards")
    public ResponseHandler<Void> removeScraps(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid ScrapDeleteRequestDto requestDto) {
        scrapService.removeScraps(principalDetails.getMemberId(), requestDto);
        return ResponseHandler.success(null);
    }

    @Operation(summary = "스크랩 목록 조회", description = "사용자가 스크랩한 게시글 목록을 조회합니다")
    @AuthenticatedApi(reason = "스크랩 목록 조회는 로그인한 사용자만 가능합니다")
    @GetMapping
    public ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> getScrapList(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute @Valid ScrapCursorRequestDto requestDto) {
        CursorTemplate<Long, BoardListResponseDto> response = scrapService.getScrapList(
                principalDetails.getMemberId(), 
                requestDto.getCursorId(), 
                requestDto.getSize()
        );
        return ResponseHandler.success(response);
    }

}