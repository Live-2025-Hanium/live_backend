package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.Enum.TargetUserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegisterCloverMissionRequestDto {

    // --- JPA DB용 정보 ---
    private String missionTitle;
    private String description;
    private MissionCategory missionCategory;
    private MissionDifficulty missionDifficulty;
    private CloverType cloverType;
    private int requiredMeters;
    private int requiredSeconds;
    private String illustrationUrl;
    private String targetAddress;

    // --- 벡터 DB용 정보 ---
    private TargetUserType targetUserType; // 가족의존형, 건강취약형 등
    private String relatedFeature; // 미션이 도움을 주고자 하는 사용자의 주요 특징
    private String activityDescription; // 미션 내용을 좀 더 자세히 설명
    private String expectedEffect; // 기대 효과
}
//미션 : missionTitle, 활동 설명: activityDescription, 도움을 줄 수 있는 사용자의 특성 : relatedFeature, 기대 효과: expectedEffect