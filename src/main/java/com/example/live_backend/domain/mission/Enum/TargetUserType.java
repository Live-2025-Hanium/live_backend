package com.example.live_backend.domain.mission.Enum;

import lombok.Getter;

@Getter
public enum TargetUserType {

    FAMILY_DEPENDENT("가족의존형"),
    HEALTH_VULNERABLE("건강취약형"),
    INDEPENDENT_LIVELIHOOD_DEBT("독립생계채무형"),
    UNEMPLOYED_POVERTY("미취업빈곤형");

    private final String description;

    TargetUserType(String description) {
        this.description = description;
    }
}
