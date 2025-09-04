package com.example.live_backend.domain.scrap.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScrapCursorRequestDto {
    
    private Long cursorId;
    
    @Min(value = 1, message = "size는 1 이상이어야 합니다")
    @Max(value = 100, message = "size는 100 이하여야 합니다")
    private int size = 20;
}