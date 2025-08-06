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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentQueryService commentQueryService;
    private final CommentLikeService commentLikeService;
    private final CommentDtoAssembler commentDtoAssembler;

    /**
     * 게시글의 댓글 목록 조회
     */
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId, Long memberId) {

        CommentQueryService.CommentStructure commentStructure = 
                commentQueryService.getCommentStructure(boardId);
        
        if (commentStructure.isEmpty()) {
            return List.of();
        }
        CommentLikeService.CommentLikeMetadata likeMetadata = 
                commentLikeService.getCommentLikeMetadata(commentStructure.getAllCommentIds(), memberId);
        return commentDtoAssembler.assembleCommentDtos(commentStructure, likeMetadata, memberId);
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


} 