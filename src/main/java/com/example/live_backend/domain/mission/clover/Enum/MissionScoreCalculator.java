package com.example.live_backend.domain.mission.clover.Enum;

import com.example.live_backend.domain.mission.clover.dto.LLMProcessingResultDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;

import java.util.List;


public enum MissionScoreCalculator {

    CATEGORY {
        @Override
        public int calculate(CloverMission mission, LLMProcessingResultDto strategy) {
            return calculateScore(
                    mission.getCategory(),
                    strategy.getRecommendCategories(),
                    strategy.getAvoidCategories()
            );
        }
    },
    DIFFICULTY {
        @Override
        public int calculate(CloverMission mission, LLMProcessingResultDto strategy) {
            return calculateScore(
                    mission.getDifficulty(),
                    strategy.getRecommendDifficulties(),
                    strategy.getAvoidDifficulties()
            );
        }
    },
    CLOVER_TYPE {
        @Override
        public int calculate(CloverMission mission, LLMProcessingResultDto strategy) {
            return calculateScore(
                    mission.getCloverType(),
                    strategy.getRecommendCloverTypes(),
                    strategy.getAvoidCloverTypes()
            );
        }
    };

    public abstract int calculate(CloverMission mission, LLMProcessingResultDto strategy);

    private static <T> int calculateScore(T value, List<T> recommendList, List<T> avoidList) {
        int score = 0;

        if (recommendList != null && recommendList.contains(value)) {
            score += 10;
        }

        if (avoidList != null && avoidList.contains(value)) {
            score -= 3;
        }

        return score;
    }
}
