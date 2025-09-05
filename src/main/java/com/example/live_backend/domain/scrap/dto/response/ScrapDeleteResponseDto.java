package com.example.live_backend.domain.scrap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapDeleteResponseDto {
    
    private int requestedCount;
    private int deletedCount;
    
    public static ScrapDeleteResponseDto of(int requestedCount, int deletedCount) {
        return ScrapDeleteResponseDto.builder()
                .requestedCount(requestedCount)
                .deletedCount(deletedCount)
                .build();
    }
}