package com.example.live_backend.domain.board.controller.docs;

import com.example.live_backend.domain.board.dto.request.CommentCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.CommentUpdateRequestDto;
import com.example.live_backend.domain.board.dto.response.CommentResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글", description = "댓글 관련 API")
public interface CommentControllerDocs {

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CommentResponseDto.class)))
    ResponseHandler<List<CommentResponseDto>> getComments(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseHandler<Long> createComment(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
            @RequestBody CommentCreateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "대댓글 작성", description = "댓글에 대댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (대댓글의 대댓글 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseHandler<Long> createReply(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
            @Parameter(description = "부모 댓글 ID", required = true) @PathVariable Long parentCommentId,
            @RequestBody CommentCreateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseHandler<Void> updateComment(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseHandler<Void> deleteComment(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "댓글 좋아요 토글", description = "댓글에 좋아요를 남기거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseHandler<Void> toggleCommentLike(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails userDetails
    );
} 