package com.example.live_backend.domain.survey.vitality.dto;

import com.example.live_backend.domain.survey.vitality.enums.IsolationAndSeclusionType;
import com.example.live_backend.domain.survey.vitality.enums.VitalityLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VitalityResultDto {
    private VitalityLevel vitalityLevel; // "고활력" 또는 "저활력"
    private IsolationAndSeclusionType isolationAndSeclusionType; // "정서적 고립", "물리적 고립", "고립+은둔", "정상" 등
    private boolean isIsolated; // 고립 청년 여부
    private boolean isSecluded; // 은둔 청년 여부
    private String description; // 판단 근거 설명
}
