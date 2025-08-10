package com.example.live_backend.domain.board.dto.response;

import com.example.live_backend.domain.board.entity.BoardImage;
import lombok.Getter;

@Getter
public class ImageResponseDto {
    private final Long id;
    private final String s3Url;

    public ImageResponseDto(BoardImage boardImage) {
        this.id = boardImage.getImage().getId();
        this.s3Url = boardImage.getImage().getS3Url();
    }
} 