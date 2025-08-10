package com.example.live_backend.domain.board.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionType {
    EMPATHY("유용해요"),
    USEFUL("힘이나요"),
    HELPFUL("도움돼요"),
    INSPIRING("공감해요"),
    ENCOURAGING("고마워요");

    private final String displayName;
} 