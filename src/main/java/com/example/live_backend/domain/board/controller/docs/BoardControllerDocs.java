package com.example.live_backend.domain.board.controller.docs;

import com.example.live_backend.domain.board.dto.request.BoardCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardReactionRequestDto;
import com.example.live_backend.domain.board.dto.response.BoardDetailResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardCategoryHomeResponseDto;
import com.example.live_backend.domain.board.dto.response.CategoryResponseDto;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "게시판", description = "게시판 관련 API")
public interface BoardControllerDocs {

	@Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다. (관리자 전용)")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "생성 성공", content = @Content(
			schema = @Schema(implementation = Long.class),
			examples = @ExampleObject(value = "1")
		)),
		@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	ResponseHandler<Long> createBoard(
		@RequestBody BoardCreateRequestDto requestDto,
		@Parameter(hidden = true)
		@AuthenticationPrincipal PrincipalDetails userDetails
	);

	@Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. (관리자 전용)")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
	})
	ResponseHandler<Void> updateBoard(
		@Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
		@RequestBody BoardUpdateRequestDto requestDto
	);

	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (관리자 전용)")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "삭제 성공"),
		@ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
	})
	ResponseHandler<Void> deleteBoard(
		@Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId
	);

	@Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 조회수가 증가됩니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
			schema = @Schema(implementation = BoardDetailResponseDto.class)
		)),
		@ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
	})
	ResponseHandler<BoardDetailResponseDto> getBoardDetail(
		@Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
		@Parameter(hidden = true)
		@AuthenticationPrincipal PrincipalDetails userDetails
	);

	@Operation(summary = "홈 화면용 카테고리별 게시글 조회", description = "각 카테고리별 최신 10개의 게시글을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공",
		content = @Content(schema = @Schema(implementation = BoardCategoryHomeResponseDto.class))
	)
	ResponseHandler<List<BoardCategoryHomeResponseDto>> getBoardsForHome();

	@Operation(summary = "게시글 검색 (커서 기반)", description = "키워드로 게시글을 검색합니다. 커서 기반 무한 스크롤을 지원합니다.")
	@ApiResponse(responseCode = "200", description = "검색 성공",
		content = @Content(schema = @Schema(implementation = CursorTemplate.class))
	)
	ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> searchBoards(
		@Parameter(description = "검색 키워드", required = true) @RequestParam String keyword,
		@Parameter(description = "커서 ID", required = false)  @RequestParam(required = false) Long cursor,
		@Parameter(description = "조회 크기", example = "20")  @RequestParam(defaultValue = "20") Integer size
	);

	@Operation(summary = "카테고리 목록 조회", description = "사용 중인 카테고리 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공",
		content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))
	)
	ResponseHandler<List<CategoryResponseDto>> getCategories();

	@Operation(summary = "게시글 반응 토글", description = "게시글에 반응을 남기거나 취소합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "토글 성공"),
		@ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
	})
	ResponseHandler<Void> toggleReaction(
		@Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
		@RequestBody BoardReactionRequestDto requestDto,
		@Parameter(hidden = true)
		@AuthenticationPrincipal PrincipalDetails userDetails
	);
}