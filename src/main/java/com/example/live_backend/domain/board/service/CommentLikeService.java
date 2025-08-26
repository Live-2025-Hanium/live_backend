package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.entity.Comment;
import com.example.live_backend.domain.board.entity.CommentLike;
import com.example.live_backend.domain.board.repository.CommentLikeRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 좋아요 메타데이터 조회
     */
    public CommentLikeMetadata getCommentLikeMetadata(List<Long> commentIds, Long memberId) {
        if (commentIds.isEmpty()) {
            return new CommentLikeMetadata(Map.of(), Set.of());
        }

        Map<Long, Long> likeCounts = commentLikeRepository.countLikesByCommentIds(commentIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Set<Long> likedCommentIds = memberId != null ?
                commentLikeRepository.findLikedCommentIdsByMemberAndCommentIds(commentIds, memberId)
                        .stream()
                        .collect(Collectors.toSet()) :
                Set.of();

        return new CommentLikeMetadata(likeCounts, likedCommentIds);
    }

    /**
     * 댓글 좋아요 토글
     */
    @Transactional
    public void toggleLike(Comment comment, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndMemberId(comment.getId(), memberId);
        
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


    public static class CommentLikeMetadata {
        private final Map<Long, Long> likeCounts;
        private final Set<Long> likedCommentIds;

        public CommentLikeMetadata(Map<Long, Long> likeCounts, Set<Long> likedCommentIds) {
            this.likeCounts = likeCounts;
            this.likedCommentIds = likedCommentIds;
        }

        public Long getLikeCount(Long commentId) {
            return likeCounts.getOrDefault(commentId, 0L);
        }

        public boolean isLiked(Long commentId) {
            return likedCommentIds.contains(commentId);
        }
    }
} 