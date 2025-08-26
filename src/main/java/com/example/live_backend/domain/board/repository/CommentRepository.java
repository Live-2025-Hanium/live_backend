package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.isDeleted = false AND c.id = :id")
    Optional<Comment> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author " +
           "WHERE c.isDeleted = false AND c.board.id = :boardId AND c.parentComment IS NULL " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findParentCommentsByBoardId(@Param("boardId") Long boardId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author " +
           "WHERE c.isDeleted = false AND c.parentComment.id IN :parentCommentIds " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentIds(@Param("parentCommentIds") List<Long> parentCommentIds);

    @Query("SELECT COUNT(c) FROM Comment c " +
           "WHERE c.isDeleted = false AND c.board.id = :boardId")
    Long countByBoardId(@Param("boardId") Long boardId);
} 