package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndMemberId(Long commentId, Long memberId);

    @Query("SELECT cl.comment.id, COUNT(cl) FROM CommentLike cl " +
           "WHERE cl.comment.id IN :commentIds " +
           "GROUP BY cl.comment.id")
    List<Object[]> countLikesByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT cl.comment.id FROM CommentLike cl " +
           "WHERE cl.comment.id IN :commentIds AND cl.member.id = :memberId")
    List<Long> findLikedCommentIdsByMemberAndCommentIds(@Param("commentIds") List<Long> commentIds, 
                                                        @Param("memberId") Long memberId);

    Long countByCommentId(Long commentId);
} 