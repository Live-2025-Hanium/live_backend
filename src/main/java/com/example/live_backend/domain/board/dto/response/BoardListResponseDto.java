package com.example.live_backend.domain.board.dto.response;


import lombok.Getter;

import java.time.LocalDateTime;

import com.example.live_backend.domain.board.entity.Board;

@Getter
public class BoardListResponseDto {
    private final Long id;
    private final String title;
    private final CategoryResponseDto category;
    private final String relatedOrganization;
    private final String thumbnailImageUrl;
    private final String authorNickname;
    private final Long viewCount;
    private final Long totalReactionCount;
    private final LocalDateTime createdAt;

    public BoardListResponseDto(Board board, String authorNickname, Long totalReactionCount) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.category = new CategoryResponseDto(board.getCategory());
        this.relatedOrganization = board.getRelatedOrganization();
        this.thumbnailImageUrl = getThumbnailUrl(board);
        this.authorNickname = authorNickname;
        this.viewCount = board.getViewCount();
        this.totalReactionCount = totalReactionCount != null ? totalReactionCount : 0L;
        this.createdAt = board.getCreatedAt();
    }
    
    private String getThumbnailUrl(Board board) {
        if (board.getBoardImages() == null || board.getBoardImages().isEmpty()) {
            return null;
        }
        
        // 첫 번째 이미지를 썸네일로 사용
        return board.getBoardImages().get(0).getImage().getS3Url();
    }
} 