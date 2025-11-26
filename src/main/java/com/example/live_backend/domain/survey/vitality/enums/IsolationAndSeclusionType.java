package com.example.live_backend.domain.survey.vitality.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IsolationAndSeclusionType {
    NORMAL("정상"),
    EMOTIONAL_ISOLATION("정서적 고립"),
    PHYSICAL_ISOLATION("물리적 고립"),
    EMOTIONAL_AND_PHYSICAL_ISOLATION("정서적+물리적 고립"),
    EMOTIONAL_ISOLATION_AND_SECLUSION("정서적 고립 + 은둔"),
    PHYSICAL_ISOLATION_AND_SECLUSION("물리적 고립 + 은둔"),
    EMOTIONAL_ISOLATION_AND_PHYSICAL_ISOLATION_AND_SECLUSION("정서적, 물리적 고립 + 은둔"),

    ISOLATION_AND_SECLUSION("고립+은둔");

    private final String description;
}
