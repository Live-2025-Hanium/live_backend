package com.example.live_backend.domain.board.dto.response;

import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.enums.ReactionType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class BoardDetailResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final CategoryResponseDto category;
    private final String relatedOrganization;
    private final List<ImageResponseDto> images;
    private final String authorNickname;
    private final Long viewCount;
    private final Long commentCount;
    private final Map<ReactionType, Long> reactionCounts;
    private final List<ReactionType> userReactions;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public BoardDetailResponseDto(Board board, String authorNickname, 
                                  Long commentCount,
                                  Map<ReactionType, Long> reactionCounts,
                                  List<ReactionType> userReactions) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.category = new CategoryResponseDto(board.getCategory());
        this.relatedOrganization = board.getRelatedOrganization();
        this.images = board.getBoardImages().stream()
                .map(ImageResponseDto::new)
                .collect(Collectors.toList());
        this.authorNickname = authorNickname;
        this.viewCount = board.getViewCount();
        this.commentCount = commentCount;
        this.reactionCounts = reactionCounts;
        this.userReactions = userReactions;
        this.createdAt = board.getCreatedAt();
        this.modifiedAt = board.getModifiedAt();
    }
} 