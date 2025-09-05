package com.example.live_backend.domain.scrap.repository;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.global.page.CursorTemplate;

public interface ScrapRepositoryCustom {
    
    CursorTemplate<Long, BoardListResponseDto> findScrapsByMemberWithCursor(Member member, Long cursorId, int size);
}