package com.example.live_backend.domain.board.dto.response;

import com.example.live_backend.domain.board.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CommentResponseDto {
    private final Long id;
    private final String content;
    private final String authorNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Long likeCount;
    private final Boolean isLiked;
    private final Boolean isMyComment;
    private final List<CommentResponseDto> replies;

    public CommentResponseDto(Comment comment, String authorNickname, Long likeCount, 
                             Boolean isLiked, Boolean isMyComment, List<CommentResponseDto> replies) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorNickname = authorNickname;
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getModifiedAt();
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.isMyComment = isMyComment;
        this.replies = replies;
    }

    public CommentResponseDto(Comment comment, String authorNickname, Long likeCount, 
                             Boolean isLiked, Boolean isMyComment) {
        this(comment, authorNickname, likeCount, isLiked, isMyComment, List.of());
    }
} 