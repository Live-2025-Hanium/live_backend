package com.example.live_backend.domain.board.controller;

import com.example.live_backend.domain.board.controller.docs.CommentControllerDocs;
import com.example.live_backend.domain.board.dto.request.CommentCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.CommentUpdateRequestDto;
import com.example.live_backend.domain.board.dto.response.CommentResponseDto;
import com.example.live_backend.domain.board.service.CommentService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import com.example.live_backend.global.security.annotation.PublicApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "댓글", description = "댓글 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDocs {

    private final CommentService commentService;

    @Override
    @PublicApi(reason = "댓글 목록 조회는 누구나 가능합니다")
    @GetMapping("/boards/{boardId}/comments")
    public ResponseHandler<List<CommentResponseDto>> getComments(
            @PathVariable Long boardId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        Long memberId = Optional.ofNullable(userDetails)
                .map(PrincipalDetails::getMemberId)
                .orElse(null);
        List<CommentResponseDto> comments = commentService.getCommentsByBoardId(boardId, memberId);
        return ResponseHandler.success(comments);
    }

    @Override
    @AuthenticatedApi(reason = "댓글 작성은 로그인한 사용자만 가능합니다")
    @PostMapping("/boards/{boardId}/comments")
    public ResponseHandler<Long> createComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CommentCreateRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        if (userDetails == null) {
            throw new com.example.live_backend.global.error.exception.CustomException(
                com.example.live_backend.global.error.exception.ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
        Long commentId = commentService.createComment(boardId, requestDto, userDetails.getMemberId());
        return ResponseHandler.success(commentId);
    }

    @Override
    @AuthenticatedApi(reason = "대댓글 작성은 로그인한 사용자만 가능합니다")
    @PostMapping("/boards/{boardId}/comments/{parentCommentId}/replies")
    public ResponseHandler<Long> createReply(
            @PathVariable Long boardId,
            @PathVariable Long parentCommentId,
            @Valid @RequestBody CommentCreateRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        if (userDetails == null) {
            throw new com.example.live_backend.global.error.exception.CustomException(
                com.example.live_backend.global.error.exception.ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
        Long replyId = commentService.createReply(boardId, parentCommentId, requestDto, userDetails.getMemberId());
        return ResponseHandler.success(replyId);
    }

    @Override
    @AuthenticatedApi(reason = "댓글 수정은 작성자만 가능합니다")
    @PutMapping("/comments/{commentId}")
    public ResponseHandler<Void> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        if (userDetails == null) {
            throw new com.example.live_backend.global.error.exception.CustomException(
                com.example.live_backend.global.error.exception.ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
        commentService.updateComment(commentId, requestDto, userDetails.getMemberId());
        return ResponseHandler.success(null);
    }

    @Override
    @AuthenticatedApi(reason = "댓글 삭제는 작성자만 가능합니다")
    @DeleteMapping("/comments/{commentId}")
    public ResponseHandler<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        if (userDetails == null) {
            throw new com.example.live_backend.global.error.exception.CustomException(
                com.example.live_backend.global.error.exception.ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
        commentService.deleteComment(commentId, userDetails.getMemberId());
        return ResponseHandler.success(null);
    }

    @Override
    @AuthenticatedApi(reason = "댓글 좋아요는 로그인한 사용자만 가능합니다")
    @PostMapping("/comments/{commentId}/likes")
    public ResponseHandler<Void> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        if (userDetails == null) {
            throw new com.example.live_backend.global.error.exception.CustomException(
                com.example.live_backend.global.error.exception.ErrorCode.DENIED_UNAUTHORIZED_USER);
        }
        commentService.toggleCommentLike(commentId, userDetails.getMemberId());
        return ResponseHandler.success(null);
    }
} 