package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import lombok.Getter;

@Getter
public class CloverMissionCreateRequestDto {

    private String missionTitle;
    private String description;
    private MissionCategory missionCategory;
    private MissionDifficulty missionDifficulty;
    private CloverType cloverType;
    private int requiredMeters;
    private int requiredSeconds;
    private String illustrationUrl;
    private String targetAddress;
}
