package com.example.live_backend.domain.scrap.repository;

import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {

    boolean existsByMemberAndBoard(Member member, Board board);

    Optional<Scrap> findByMemberAndBoard(Member member, Board board);
    
    @Query("SELECT s FROM Scrap s WHERE s.member.id = :memberId AND s.board.id = :boardId")
    Optional<Scrap> findByMemberIdAndBoardId(@Param("memberId") Long memberId, @Param("boardId") Long boardId);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Scrap s WHERE s.member.id = :memberId AND s.board.id = :boardId")
    boolean existsByMemberIdAndBoardId(@Param("memberId") Long memberId, @Param("boardId") Long boardId);

    @Modifying
    @Query("DELETE FROM Scrap s WHERE s.member = :member AND s.board.id IN :boardIds")
    void deleteByMemberAndBoardIds(@Param("member") Member member, @Param("boardIds") List<Long> boardIds);

}