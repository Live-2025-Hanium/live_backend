package com.example.live_backend.domain.board.dto.response;

import lombok.Getter;
import java.util.List;

@Getter
public class BoardCategoryHomeResponseDto {
    private final String category;
    private final List<BoardListResponseDto> boards;

    public BoardCategoryHomeResponseDto(String category, List<BoardListResponseDto> boards) {
        this.category = category;
        this.boards = boards;
    }
} 