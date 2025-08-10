package com.example.live_backend.domain.board.controller;

import com.example.live_backend.domain.board.controller.docs.BoardControllerDocs;
import com.example.live_backend.domain.board.dto.request.BoardCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardReactionRequestDto;
import com.example.live_backend.domain.board.dto.response.BoardDetailResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardCategoryHomeResponseDto;
import com.example.live_backend.domain.board.dto.response.CategoryResponseDto;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.annotation.AdminApi;
import com.example.live_backend.global.security.annotation.PublicApi;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.domain.board.service.BoardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Tag(name = "게시판", description = "게시판 관련 API")
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController implements BoardControllerDocs {

	private final BoardService boardService;

	@Override
	@AdminApi(reason = "게시글 작성은 관리자만 가능합니다")
	@PostMapping
	public ResponseHandler<Long> createBoard(
		@Valid @RequestBody BoardCreateRequestDto requestDto,
		@AuthenticationPrincipal PrincipalDetails userDetails) {
		Long boardId = boardService.createBoard(requestDto, userDetails.getMemberId());
		return ResponseHandler.success(boardId);
	}

	@Override
	@AdminApi(reason = "게시글 수정은 관리자만 가능합니다")
	@PutMapping("/{boardId}")
	public ResponseHandler<Void> updateBoard(
		@PathVariable Long boardId,
		@Valid @RequestBody BoardUpdateRequestDto requestDto) {
		boardService.updateBoard(boardId, requestDto);
		return ResponseHandler.success(null);
	}

	@Override
	@AdminApi(reason = "게시글 삭제는 관리자만 가능합니다")
	@DeleteMapping("/{boardId}")
	public ResponseHandler<Void> deleteBoard(@PathVariable Long boardId) {
		boardService.deleteBoard(boardId);
		return ResponseHandler.success(null);
	}

	@Override
	@PublicApi(reason = "게시글 상세 조회는 누구나 가능합니다")
	@GetMapping("/{boardId}")
	public ResponseHandler<BoardDetailResponseDto> getBoardDetail(
		@PathVariable Long boardId,
		@AuthenticationPrincipal PrincipalDetails userDetails) {
		Long memberId = Optional.ofNullable(userDetails)
			.map(PrincipalDetails::getMemberId)
			.orElse(null);
		BoardDetailResponseDto response = boardService.getBoardDetail(boardId, memberId);
		return ResponseHandler.success(response);
	}

	@Override
	@PublicApi(reason = "홈 화면 게시글 조회는 누구나 가능합니다")
	@GetMapping("/home")
	public ResponseHandler<List<BoardCategoryHomeResponseDto>> getBoardsForHome(
		@RequestParam(defaultValue = "latest") String sortBy) {
		List<BoardCategoryHomeResponseDto> response = boardService.getBoardsForHome(sortBy);
		return ResponseHandler.success(response);
	}

	@Override
	@PublicApi(reason = "게시글 검색은 누구나 가능합니다")
	@GetMapping("/search")
	public ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> searchBoards(
		@RequestParam String keyword,
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "20") Integer size,
		@RequestParam(defaultValue = "latest") String sortBy) {
		CursorTemplate<Long, BoardListResponseDto> response =
			boardService.searchBoardsWithCursor(keyword, cursor, size, sortBy);
		return ResponseHandler.success(response);
	}

	@Override
	@PublicApi(reason = "카테고리 목록 조회는 누구나 가능합니다")
	@GetMapping("/categories")
	public ResponseHandler<List<CategoryResponseDto>> getCategories() {
		List<CategoryResponseDto> categories = boardService.getCategories();
		return ResponseHandler.success(categories);
	}

	@Override
	@AuthenticatedApi(reason = "반응 남기기는 로그인한 사용자만 가능합니다")
	@PostMapping("/{boardId}/reactions")
	public ResponseHandler<Void> toggleReaction(
		@PathVariable Long boardId,
		@Valid @RequestBody BoardReactionRequestDto requestDto,
		@AuthenticationPrincipal PrincipalDetails userDetails) {
		boardService.toggleReaction(boardId, userDetails.getMemberId(), requestDto.getReactionType());
		return ResponseHandler.success(null);
	}
}
