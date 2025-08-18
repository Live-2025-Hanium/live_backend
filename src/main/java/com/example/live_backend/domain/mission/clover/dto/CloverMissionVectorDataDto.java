package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.TargetUserType;
import lombok.Getter;

@Getter
public class CloverMissionVectorDataDto {

    private TargetUserType targetUserType; // 가족의존형, 건강취약형 등
    private String relatedFeature; // 미션이 도움을 주고자 하는 사용자의 주요 특징
    private String activityDescription; // 미션 내용을 좀 더 자세히 설명
    private String expectedEffect; // 기대 효과
}
