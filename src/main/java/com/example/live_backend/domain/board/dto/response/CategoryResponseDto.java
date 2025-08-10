package com.example.live_backend.domain.board.dto.response;

import com.example.live_backend.domain.board.entity.Category;
import lombok.Getter;

@Getter
public class CategoryResponseDto {
    private final Long id;
    private final String name;

    public CategoryResponseDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
} 