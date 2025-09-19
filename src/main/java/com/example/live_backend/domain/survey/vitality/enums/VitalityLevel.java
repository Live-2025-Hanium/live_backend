package com.example.live_backend.domain.survey.vitality.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VitalityLevel {
    NORMAL("정상"),
    HIGH_VITALITY("고활력"),
    LOW_VITALITY("저활력");

    private final String description;
}
