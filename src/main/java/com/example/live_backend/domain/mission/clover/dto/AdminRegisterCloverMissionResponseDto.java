package com.example.live_backend.domain.mission.clover.dto;

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
public class AdminRegisterCloverMissionResponseDto {

    private Long cloverMissionId;
    private String missionTitle;
    private String description;
    private MissionCategory missionCategory;
    private MissionDifficulty missionDifficulty;
    private TargetUserType targetUserType;
    private String vectorDocument;
}


