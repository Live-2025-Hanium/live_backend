package com.example.live_backend.domain.board.service;


import com.example.live_backend.domain.board.dto.response.CommentResponseDto;
import com.example.live_backend.domain.board.entity.Comment;
import com.example.live_backend.domain.memeber.entity.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentDtoAssembler {


    public List<CommentResponseDto> assembleCommentDtos(
            CommentQueryService.CommentStructure commentStructure,
            CommentLikeService.CommentLikeMetadata likeMetadata,
            Long currentUserId) {

        if (commentStructure.isEmpty()) {
            return List.of();
        }

        return commentStructure.getParentComments().stream()
                .map(parentComment -> {

                    List<CommentResponseDto> replyDtos = commentStructure.getReplies(parentComment.getId())
                            .stream()
                            .map(reply -> createCommentDto(reply, likeMetadata, currentUserId))
                            .toList();

                    return createCommentDto(parentComment, likeMetadata, currentUserId, replyDtos);
                })
                .toList();
    }

    private CommentResponseDto createCommentDto(
            Comment comment,
            CommentLikeService.CommentLikeMetadata likeMetadata,
            Long currentUserId) {
        
        return new CommentResponseDto(
                comment,
                getAuthorNickname(comment.getAuthor()),
                likeMetadata.getLikeCount(comment.getId()),
                likeMetadata.isLiked(comment.getId()),
                isMyComment(comment, currentUserId)
        );
    }

    private CommentResponseDto createCommentDto(
            Comment comment,
            CommentLikeService.CommentLikeMetadata likeMetadata,
            Long currentUserId,
            List<CommentResponseDto> replies) {
        
        return new CommentResponseDto(
                comment,
                getAuthorNickname(comment.getAuthor()),
                likeMetadata.getLikeCount(comment.getId()),
                likeMetadata.isLiked(comment.getId()),
                isMyComment(comment, currentUserId),
                replies
        );
    }

    private String getAuthorNickname(Member author) {
        return author.getProfile() != null ? author.getProfile().getNickname() : "알 수 없음";
    }

    private boolean isMyComment(Comment comment, Long currentUserId) {
        return currentUserId != null && comment.getAuthor().getId().equals(currentUserId);
    }
} 