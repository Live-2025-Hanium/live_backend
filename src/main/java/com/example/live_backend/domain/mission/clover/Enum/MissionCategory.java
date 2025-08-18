package com.example.live_backend.domain.mission.clover.Enum;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MissionCategory {

    COMMUNICATION("소통하기"),
    RELATIONSHIP("인간관계 챙기기"),
    ENVIRONMENT("환경 바꾸기"),
    HEALTH("건강 챙기기");

    private final String inKr;

    MissionCategory(String inKr) {
        this.inKr = inKr;
    }

    public static MissionCategory fromKo(String koLabel) {
        return Arrays.stream(values())
                .filter(c -> c.inKr.equals(koLabel))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CATEGORY));
    }
}