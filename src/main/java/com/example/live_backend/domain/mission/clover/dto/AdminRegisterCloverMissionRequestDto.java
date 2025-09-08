package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegisterCloverMissionRequestDto {

    private String missionTitle;
    private String description;
    private MissionCategory missionCategory;
    private MissionDifficulty missionDifficulty;
    private CloverType cloverType;
    private int requiredMeters;
    private int requiredSeconds;
    private String illustrationUrl;
    private String targetAddress;

    private String relatedFeature;
    private String activityDescription;
    private String expectedEffect;
}
//미션 : missionTitle, 활동 설명: activityDescription, 도움을 줄 수 있는 사용자의 특성 : relatedFeature, 기대 효과: expectedEffect