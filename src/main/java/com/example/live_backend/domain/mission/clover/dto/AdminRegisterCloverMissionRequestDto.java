package com.example.live_backend.domain.mission.clover.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegisterCloverMissionRequestDto {

    private CloverMissionCreateRequestDto cloverMissionCreateRequestDto;
    private CloverMissionVectorDataDto cloverMissionVectorDataDto;
}
//미션 : missionTitle, 활동 설명: activityDescription, 도움을 줄 수 있는 사용자의 특성 : relatedFeature, 기대 효과: expectedEffect