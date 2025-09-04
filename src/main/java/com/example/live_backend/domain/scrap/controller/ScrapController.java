package com.example.live_backend.domain.scrap.controller;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.scrap.controller.docs.ScrapControllerDocs;
import com.example.live_backend.domain.scrap.dto.request.ScrapCursorRequestDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapToggleResponseDto;
import com.example.live_backend.domain.scrap.service.ScrapService;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scraps")
@RequiredArgsConstructor
public class ScrapController implements ScrapControllerDocs {

    private final ScrapService scrapService;

    @AuthenticatedApi(reason = "스크랩은 로그인한 사용자만 가능합니다")
    @PostMapping("/boards/{boardId}/toggle")
    @Override
    public ResponseHandler<ScrapToggleResponseDto> toggleScrap(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long boardId) {
        boolean isScraped = scrapService.toggleScrap(principalDetails.getMemberId(), boardId);
        return ResponseHandler.success(ScrapToggleResponseDto.of(isScraped));
    }

    @AuthenticatedApi(reason = "스크랩 취소는 로그인한 사용자만 가능합니다")
    @DeleteMapping("/boards")
    @Override
    public ResponseHandler<Void> removeScraps(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid ScrapDeleteRequestDto requestDto) {
        scrapService.removeScraps(principalDetails.getMemberId(), requestDto);
        return ResponseHandler.success(null);
    }

    @AuthenticatedApi(reason = "스크랩 목록 조회는 로그인한 사용자만 가능합니다")
    @GetMapping
    @Override
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