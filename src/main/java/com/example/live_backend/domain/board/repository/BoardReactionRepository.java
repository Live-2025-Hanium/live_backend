package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.BoardReaction;
import com.example.live_backend.domain.board.entity.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardReactionRepository extends JpaRepository<BoardReaction, Long> {

    @Query("SELECT br FROM BoardReaction br WHERE br.board.id = :boardId AND br.member.id = :memberId AND br.reactionType = :reactionType")
    Optional<BoardReaction> findByBoardIdAndMemberIdAndReactionType(
            @Param("boardId") Long boardId, 
            @Param("memberId") Long memberId, 
            @Param("reactionType") ReactionType reactionType);

    @Query("SELECT br FROM BoardReaction br WHERE br.board.id = :boardId AND br.member.id = :memberId AND br.deletedAt IS NULL")
    List<BoardReaction> findActiveReactionsByBoardIdAndMemberId(@Param("boardId") Long boardId, @Param("memberId") Long memberId);

    @Query("SELECT br.reactionType as reactionType, COUNT(br) as count " +
           "FROM BoardReaction br " +
           "WHERE br.board.id = :boardId AND br.deletedAt IS NULL " +
           "GROUP BY br.reactionType")
    List<ReactionCount> countReactionsByBoardId(@Param("boardId") Long boardId);

    @Query("SELECT br.board.id as boardId, br.reactionType as reactionType, COUNT(br) as count " +
           "FROM BoardReaction br " +
           "WHERE br.board.id IN :boardIds AND br.deletedAt IS NULL " +
           "GROUP BY br.board.id, br.reactionType")
    List<BoardReactionCount> countReactionsByBoardIds(@Param("boardIds") List<Long> boardIds);


    interface ReactionCount {
        ReactionType getReactionType();
        Long getCount();
    }

    interface BoardReactionCount {
        Long getBoardId();
        ReactionType getReactionType();
        Long getCount();
    }
} 