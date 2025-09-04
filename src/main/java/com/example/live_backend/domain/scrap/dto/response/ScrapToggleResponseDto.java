package com.example.live_backend.domain.scrap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScrapToggleResponseDto {
    private final boolean isScraped;
    private final String message;
    
    public static ScrapToggleResponseDto of(boolean isScraped) {
        String message = isScraped ? "스크랩이 추가되었습니다." : "스크랩이 취소되었습니다.";
        return new ScrapToggleResponseDto(isScraped, message);
    }
}