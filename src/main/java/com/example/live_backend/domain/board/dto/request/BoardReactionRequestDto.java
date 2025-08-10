package com.example.live_backend.domain.board.dto.request;

import com.example.live_backend.domain.board.entity.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardReactionRequestDto {

    @NotNull(message = "반응 타입은 필수입니다.")
    private ReactionType reactionType;

    public BoardReactionRequestDto(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
} 