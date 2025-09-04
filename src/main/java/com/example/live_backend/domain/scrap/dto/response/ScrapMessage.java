package com.example.live_backend.domain.scrap.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScrapMessage {
    SCRAP_ADDED("스크랩이 추가되었습니다."),
    SCRAP_REMOVED("스크랩이 취소되었습니다.");

    private final String message;
}