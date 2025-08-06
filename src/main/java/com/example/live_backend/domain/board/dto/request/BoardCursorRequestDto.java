package com.example.live_backend.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardCursorRequestDto {

    @Schema(description = "커서 (다음 페이지 조회 시 사용, 첫 페이지는 null)", example = "123")
    private Long cursor;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;

    public BoardCursorRequestDto(Long cursor, Integer size) {
        this.cursor = cursor;
        this.size = size != null ? size : 20;
    }
} 