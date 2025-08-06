package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.dto.request.CommentCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.CommentUpdateRequestDto;
import com.example.live_backend.domain.board.dto.response.CommentResponseDto;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.Comment;
import com.example.live_backend.domain.board.entity.CommentLike;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.board.repository.CommentLikeRepository;
import com.example.live_backend.domain.board.repository.CommentRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    /**
     * 게시글의 댓글 목록 조회
     */
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId, Long memberId) {
        // 부모 댓글들 조회
        List<Comment> parentComments = commentRepository.findParentCommentsByBoardId(boardId);
        
        if (parentComments.isEmpty()) {
            return List.of();
        }

        // 모든 댓글 ID 수집 (부모 + 자식)
        List<Long> parentCommentIds = parentComments.stream()
                .map(Comment::getId)
                .toList();
        
        List<Comment> allReplies = parentCommentIds.stream()
                .flatMap(parentId -> commentRepository.findRepliesByParentCommentId(parentId).stream())
                .toList();
        
        List<Long> allCommentIds = parentComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        allCommentIds.addAll(allReplies.stream().map(Comment::getId).toList());

        // 좋아요 수 조회
        Map<Long, Long> likeCounts = getLikeCounts(allCommentIds);
        
        // 현재 사용자의 좋아요 여부 조회
        List<Long> likedCommentIds = memberId != null ? 
                commentLikeRepository.findLikedCommentIdsByMemberAndCommentIds(allCommentIds, memberId) : 
                List.of();

        // 부모 댓글들을 DTO로 변환
        return parentComments.stream()
                .map(parentComment -> {
                    // 해당 부모 댓글의 대댓글들 조회
                    List<Comment> replies = commentRepository.findRepliesByParentCommentId(parentComment.getId());
                    
                    // 대댓글들을 DTO로 변환
                    List<CommentResponseDto> replyDtos = replies.stream()
                            .map(reply -> new CommentResponseDto(
                                    reply,
                                    getAuthorNickname(reply.getAuthor()),
                                    likeCounts.getOrDefault(reply.getId(), 0L),
                                    likedCommentIds.contains(reply.getId()),
                                    memberId != null && reply.getAuthor().getId().equals(memberId)
                            ))
                            .toList();

                    return new CommentResponseDto(
                            parentComment,
                            getAuthorNickname(parentComment.getAuthor()),
                            likeCounts.getOrDefault(parentComment.getId(), 0L),
                            likedCommentIds.contains(parentComment.getId()),
                            memberId != null && parentComment.getAuthor().getId().equals(memberId),
                            replyDtos
                    );
                })
                .toList();
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
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndMemberId(commentId, memberId);
        
        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
        } else {
            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .member(member)
                    .build();
            commentLikeRepository.save(commentLike);
        }
    }

    private Map<Long, Long> getLikeCounts(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }
        
        return commentLikeRepository.countLikesByCommentIds(commentIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private String getAuthorNickname(Member author) {
        return author.getProfile() != null ? author.getProfile().getNickname() : "알 수 없음";
    }
} 