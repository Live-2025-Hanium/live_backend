package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {

    @Query("SELECT b FROM Board b WHERE b.isDeleted = false AND b.id = :id")
    Optional<Board> findByIdAndNotDeleted(@Param("id") Long id);



    @Query("SELECT b FROM Board b JOIN FETCH b.category WHERE b.isDeleted = false AND b.category.name = :categoryName " +
           "AND (:cursor IS NULL OR b.id < :cursor) " +
           "ORDER BY b.id DESC")
    List<Board> findByCategoryWithCursor(@Param("categoryName") String categoryName, 
                                        @Param("cursor") Long cursor, 
                                        Pageable pageable);

    @Query("SELECT b FROM Board b JOIN FETCH b.category WHERE b.isDeleted = false AND b.category.name = :categoryName " +
           "ORDER BY b.createdAt DESC")
    List<Board> findLatestBoardsByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT b FROM Board b JOIN FETCH b.category WHERE b.isDeleted = false AND b.category.name = :categoryName " +
           "ORDER BY b.viewCount DESC, b.createdAt DESC")
    List<Board> findBoardsByCategoryOrderByViews(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT b FROM Board b JOIN FETCH b.category WHERE b.isDeleted = false AND b.category.name = :categoryName " +
           "AND (:cursor IS NULL OR b.viewCount < :cursor OR (b.viewCount = :cursor AND b.id < :cursorId)) " +
           "ORDER BY b.viewCount DESC, b.id DESC")
    List<Board> findByCategoryWithCursorOrderByViews(@Param("categoryName") String categoryName, 
                                                     @Param("cursor") Long cursor,
                                                     @Param("cursorId") Long cursorId, 
                                                     Pageable pageable);

} 