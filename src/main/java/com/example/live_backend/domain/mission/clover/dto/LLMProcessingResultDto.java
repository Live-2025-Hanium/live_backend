package com.example.live_backend.domain.mission.clover.dto;

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

    @JsonProperty("negative_keywords")
    private List<String> negativeKeywords;

    @JsonProperty("expected_effect")
    private String expectedEffect;
}
