package com.example.live_backend.global.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadType {
    PROFILE("profile-image"),
    BOARD("board-image");

    private final String dir;
}
