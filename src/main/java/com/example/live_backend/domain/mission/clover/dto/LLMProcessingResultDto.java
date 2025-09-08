package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LLMProcessingResultDto {

    @JsonProperty("search_query")
    private String searchQuery;

    @JsonProperty("recommend_categories")
    private List<MissionCategory> recommendCategories;

    @JsonProperty("avoid_categories")
    private List<MissionCategory> avoidCategories;

    @JsonProperty("recommend_difficulties")
    private List<MissionDifficulty> recommendDifficulties;

    @JsonProperty("avoid_difficulties")
    private List<MissionDifficulty> avoidDifficulties;

    @JsonProperty("recommend_clover_types")
    private List<CloverType> recommendCloverTypes;

    @JsonProperty("avoid_clover_types")
    private List<CloverType> avoidCloverTypes;
}
