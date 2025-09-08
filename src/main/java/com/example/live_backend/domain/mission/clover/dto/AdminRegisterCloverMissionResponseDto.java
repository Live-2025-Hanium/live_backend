package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
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
    private CloverType cloverType;
    private String vectorDocument;

    public static AdminRegisterCloverMissionResponseDto from(CloverMission mission, String vectorDocument) {
        return AdminRegisterCloverMissionResponseDto.builder()
                .cloverMissionId(mission.getId())
                .missionTitle(mission.getTitle())
                .description(mission.getDescription())
                .missionCategory(mission.getCategory())
                .missionDifficulty(mission.getDifficulty())
                .cloverType(mission.getCloverType())
                .vectorDocument(vectorDocument)
                .build();
    }
}


