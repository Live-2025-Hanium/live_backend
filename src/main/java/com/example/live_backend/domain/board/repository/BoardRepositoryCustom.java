package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.Board;


import java.util.List;

public interface BoardRepositoryCustom {
    List<Board> searchBoardsWithCursor(String keyword, Long cursor, int size);
    List<Board> searchBoardsWithCursorOrderByViews(String keyword, Long viewCountCursor, Long idCursor, int size);
} 