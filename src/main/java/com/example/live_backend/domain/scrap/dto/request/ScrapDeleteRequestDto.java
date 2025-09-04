package com.example.live_backend.domain.scrap.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ScrapDeleteRequestDto {
    
    @NotEmpty(message = "삭제할 게시글 ID를 입력해주세요")
    @Size(max = 100, message = "한 번에 최대 100개까지 삭제 가능합니다")
    private List<Long> boardIds;
}