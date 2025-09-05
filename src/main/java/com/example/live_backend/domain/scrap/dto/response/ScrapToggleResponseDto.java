package com.example.live_backend.domain.scrap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScrapToggleResponseDto {
    private final boolean isScraped;
    private final String message;
    
    public static ScrapToggleResponseDto of(boolean isScraped) {
        String message = isScraped ? ScrapMessage.SCRAP_ADDED.getMessage() : ScrapMessage.SCRAP_REMOVED.getMessage();
        return new ScrapToggleResponseDto(isScraped, message);
    }
}