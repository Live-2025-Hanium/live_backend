package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.dto.request.CommentCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.CommentUpdateRequestDto;
import com.example.live_backend.domain.board.dto.response.CommentResponseDto;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.Comment;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.board.repository.CommentRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeService commentLikeService;


    public List<CommentResponseDto> getCommentsByBoardId(Long boardId, Long memberId) {
        List<Comment> parentComments = commentRepository.findParentCommentsByBoardId(boardId);
        
        if (parentComments.isEmpty()) {
            return List.of();
        }
        List<Long> parentCommentIds = parentComments.stream()
                .map(Comment::getId)
                .toList();

        List<Comment> allReplies = commentRepository.findRepliesByParentCommentIds(parentCommentIds);
        
        Map<Long, List<Comment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParentComment().getId()));

        Set<Long> allCommentIds = new HashSet<>();
        parentComments.forEach(comment -> allCommentIds.add(comment.getId()));
        allReplies.forEach(reply -> allCommentIds.add(reply.getId()));

        CommentLikeService.CommentLikeMetadata likeMetadata = 
                commentLikeService.getCommentLikeMetadata(new ArrayList<>(allCommentIds), memberId);

        return parentComments.stream()
                .map(parentComment -> {
                    List<CommentResponseDto> replyDtos = repliesMap.getOrDefault(parentComment.getId(), List.of())
                            .stream()
                            .map(reply -> createCommentDto(reply, likeMetadata, memberId))
                            .toList();

                    return createCommentDto(parentComment, likeMetadata, memberId, replyDtos);
                })
                .toList();
    }
    

    private CommentResponseDto createCommentDto(
            Comment comment,
            CommentLikeService.CommentLikeMetadata likeMetadata,
            Long currentUserId) {
        
        String authorNickname = comment.getAuthor().getProfile() != null ? 
                comment.getAuthor().getProfile().getNickname() : "알 수 없음";
        
        return new CommentResponseDto(
                comment,
                authorNickname,
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
        
        String authorNickname = comment.getAuthor().getProfile() != null ? 
                comment.getAuthor().getProfile().getNickname() : "알 수 없음";
        
        return new CommentResponseDto(
                comment,
                authorNickname,
                likeMetadata.getLikeCount(comment.getId()),
                likeMetadata.isLiked(comment.getId()),
                isMyComment(comment, currentUserId),
                replies
        );
    }
    
    /**
     * 댓글 작성자 확인
     */
    private boolean isMyComment(Comment comment, Long currentUserId) {
        return currentUserId != null && comment.getAuthor().getId().equals(currentUserId);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public Long createComment(Long boardId, CommentCreateRequestDto requestDto, Long authorId) {
        Board board = boardRepository.findByIdAndNotDeleted(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .board(board)
                .author(author)
                .parentComment(null)
                .build();

        return commentRepository.save(comment).getId();
    }

    /**
     * 대댓글 작성
     */
    @Transactional
    public Long createReply(Long boardId, Long parentCommentId, CommentCreateRequestDto requestDto, Long authorId) {
        Board board = boardRepository.findByIdAndNotDeleted(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        Comment parentComment = commentRepository.findByIdAndNotDeleted(parentCommentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!parentComment.getBoard().getId().equals(boardId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (parentComment.isReply()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        
        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Comment reply = Comment.builder()
                .content(requestDto.getContent())
                .board(board)
                .author(author)
                .parentComment(parentComment)
                .build();

        return commentRepository.save(reply).getId();
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequestDto requestDto, Long memberId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getAuthor().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_FORBIDDEN);
        }
        
        comment.updateContent(requestDto.getContent());
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getAuthor().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_FORBIDDEN);
        }
        
        comment.delete();
    }

    /**
     * 댓글 좋아요 토글
     */
    @Transactional
    public void toggleCommentLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        
        commentLikeService.toggleLike(comment, memberId);
    }


} 