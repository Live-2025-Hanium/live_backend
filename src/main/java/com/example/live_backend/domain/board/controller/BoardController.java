package com.example.live_backend.domain.board.controller;

import com.example.live_backend.domain.board.dto.request.BoardCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardUpdateRequestDto;

import com.example.live_backend.domain.board.dto.response.BoardDetailResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardCategoryHomeResponseDto;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.domain.board.service.BoardService;
import com.example.live_backend.domain.board.dto.response.CategoryResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AdminApi;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import com.example.live_backend.global.security.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "게시판", description = "게시판 관련 API")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @AdminApi(reason = "게시글 작성은 관리자만 가능합니다")
    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다. (관리자 전용)")
    @PostMapping
    public ResponseHandler<Long> createBoard(
            @Valid @RequestBody BoardCreateRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        
        Long boardId = boardService.createBoard(requestDto, userDetails.getMemberId());
        return ResponseHandler.success(boardId);
    }

    @AdminApi(reason = "게시글 수정은 관리자만 가능합니다")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. (관리자 전용)")
    @PutMapping("/{boardId}")
    public ResponseHandler<Void> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequestDto requestDto) {
        
        boardService.updateBoard(boardId, requestDto);
        return ResponseHandler.success(null);
    }

    @AdminApi(reason = "게시글 삭제는 관리자만 가능합니다")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (관리자 전용)")
    @DeleteMapping("/{boardId}")
    public ResponseHandler<Void> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ResponseHandler.success(null);
    }

    @PublicApi(reason = "게시글 상세 조회는 누구나 가능합니다")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 조회수가 증가됩니다.")
    @GetMapping("/{boardId}")
    public ResponseHandler<BoardDetailResponseDto> getBoardDetail(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails) {
        
        Long memberId = Optional.ofNullable(userDetails)
                .map(PrincipalDetails::getMemberId)
                .orElse(null);
        
        BoardDetailResponseDto response = boardService.getBoardDetail(boardId, memberId);
        return ResponseHandler.success(response);
    }

    @PublicApi(reason = "홈 화면 게시글 조회는 누구나 가능합니다")
    @Operation(summary = "홈 화면용 카테고리별 게시글 조회", description = "각 카테고리별로 최신 10개의 게시글을 조회합니다.")
    @GetMapping("/home")
    public ResponseHandler<List<BoardCategoryHomeResponseDto>> getBoardsForHome() {
        List<BoardCategoryHomeResponseDto> response = boardService.getBoardsForHome();
        return ResponseHandler.success(response);
    }


    @PublicApi(reason = "게시글 검색은 누구나 가능합니다")
    @Operation(summary = "게시글 검색 (커서 기반)", description = "키워드로 게시글을 검색합니다. 커서 기반 무한 스크롤을 지원합니다.")
    @GetMapping("/search")
    public ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> searchBoards(
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") Integer size) {
        
        CursorTemplate<Long, BoardListResponseDto> response = 
                boardService.searchBoardsWithCursor(keyword, cursor, size);
        return ResponseHandler.success(response);
    }



	//현재 기획상으로는 필요없는 api지만 일단 만들어놓았습니다.
    @PublicApi(reason = "카테고리 목록 조회는 누구나 가능합니다")
    @Operation(summary = "카테고리 목록 조회", description = "사용 중인 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    public ResponseHandler<List<CategoryResponseDto>> getCategories() {
        List<CategoryResponseDto> categories = boardService.getCategories();
        return ResponseHandler.success(categories);
    }

    @AuthenticatedApi(reason = "반응 남기기는 로그인한 사용자만 가능합니다")
    @Operation(summary = "게시글 반응 토글", 
               description = "게시글에 반응을 남기거나 취소합니다. " +
                           "같은 반응을 다시 누르면 취소되고, " +
                           "다른 반응을 누르면 기존 반응이 새로운 반응으로 변경됩니다.")
    @PostMapping("/{boardId}/reactions")
    public ResponseHandler<Void> toggleReaction(
            @PathVariable Long boardId,
            @Valid @RequestBody com.example.live_backend.domain.board.dto.request.BoardReactionRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        
        boardService.toggleReaction(boardId, userDetails.getMemberId(), requestDto.getReactionType());
        return ResponseHandler.success(null);
    }
} 