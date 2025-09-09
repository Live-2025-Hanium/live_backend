package com.example.live_backend.domain.mission.clover.Enum;

import com.example.live_backend.domain.mission.clover.dto.LLMProcessingResultDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("미션 점수 계산기(MissionScoreCalculator) 테스트")
class MissionScoreCalculatorTest {

    private CloverMission mockMission;

    @BeforeEach
    void setUp() {
        mockMission = mock(CloverMission.class);
    }

    @Nested
    @DisplayName("카테고리(CATEGORY) 점수 계산")
    class CategoryScoreTests {

        @Test
        @DisplayName("추천 카테고리에 해당하면 +10점을 반환한다")
        void calculate_shouldReturn10_whenCategoryIsRecommended() {

            // ---Given---
            when(mockMission.getCategory()).thenReturn(MissionCategory.HEALTH);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendCategories(List.of(MissionCategory.HEALTH, MissionCategory.COMMUNICATION))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CATEGORY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(10);
        }

        @Test
        @DisplayName("회피 카테고리에 해당하면 -5점을 반환한다")
        void calculate_shouldReturnMinus5_whenCategoryIsAvoided() {

            // ---Given---
            when(mockMission.getCategory()).thenReturn(MissionCategory.ENVIRONMENT);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .avoidCategories(List.of(MissionCategory.ENVIRONMENT))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CATEGORY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(-5);
        }

        @Test
        @DisplayName("추천과 회피 카테고리에 모두 해당하면 +5점을 반환한다")
        void calculate_shouldReturn5_whenCategoryIsBothRecommendedAndAvoided() {

            // ---Given---
            when(mockMission.getCategory()).thenReturn(MissionCategory.HEALTH);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendCategories(List.of(MissionCategory.HEALTH))
                    .avoidCategories(List.of(MissionCategory.HEALTH))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CATEGORY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(5);
        }

        @Test
        @DisplayName("추천이나 회피 카테고리에 해당하지 않으면 0점을 반환한다")
        void calculate_shouldReturn0_whenCategoryIsNotInLists() {

            // ---Given---
            when(mockMission.getCategory()).thenReturn(MissionCategory.RELATIONSHIP);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendCategories(List.of(MissionCategory.HEALTH))
                    .avoidCategories(List.of(MissionCategory.ENVIRONMENT))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CATEGORY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isZero();
        }

        @Test
        @DisplayName("전략 리스트가 null일 경우에도 NullPointerException 없이 0점을 반환한다")
        void calculate_shouldReturn0_whenStrategyListsAreNull() {

            // ---Given---
            when(mockMission.getCategory()).thenReturn(MissionCategory.HEALTH);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendCategories(null)
                    .avoidCategories(null)
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CATEGORY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isZero();
        }
    }

    @Nested
    @DisplayName("난이도(DIFFICULTY) 점수 계산")
    class DifficultyScoreTests {

        @Test
        @DisplayName("추천 난이도에 해당하면 +10점을 반환한다")
        void calculate_shouldReturn10_whenDifficultyIsRecommended() {

            // ---Given---
            when(mockMission.getDifficulty()).thenReturn(MissionDifficulty.EASY);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendDifficulties(List.of(MissionDifficulty.EASY, MissionDifficulty.VERY_EASY))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.DIFFICULTY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(10);
        }

        @Test
        @DisplayName("회피 난이도에 해당하면 -5점을 반환한다")
        void calculate_shouldReturnMinus5_whenDifficultyIsAvoided() {

            // ---Given---
            when(mockMission.getDifficulty()).thenReturn(MissionDifficulty.HARD);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .avoidDifficulties(List.of(MissionDifficulty.HARD, MissionDifficulty.VERY_HARD))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.DIFFICULTY.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(-5);
        }
    }

    @Nested
    @DisplayName("클로버 타입(CLOVER_TYPE) 점수 계산")
    class CloverTypeScoreTests {

        @Test
        @DisplayName("추천 타입에 해당하면 +10점을 반환한다")
        void calculate_shouldReturn10_whenCloverTypeIsRecommended() {

            // ---Given---
            when(mockMission.getCloverType()).thenReturn(CloverType.PHOTO);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendCloverTypes(List.of(CloverType.PHOTO, CloverType.VISIT))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CLOVER_TYPE.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(10);
        }

        @Test
        @DisplayName("회피 타입에 해당하면 -5점을 반환한다")
        void calculate_shouldReturnMinus5_whenCloverTypeIsAvoided() {

            // ---Given---
            when(mockMission.getCloverType()).thenReturn(CloverType.DISTANCE);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .avoidCloverTypes(List.of(CloverType.DISTANCE))
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CLOVER_TYPE.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isEqualTo(-5);
        }

        @Test
        @DisplayName("전략 리스트가 비어있을 때 0점을 반환한다")
        void calculate_shouldReturn0_whenStrategyListsAreEmpty() {

            // ---Given---
            when(mockMission.getCloverType()).thenReturn(CloverType.TIMER);
            LLMProcessingResultDto strategy = LLMProcessingResultDto.builder()
                    .recommendCloverTypes(Collections.emptyList())
                    .avoidCloverTypes(Collections.emptyList())
                    .build();

            // ---When---
            int score = MissionScoreCalculator.CLOVER_TYPE.calculate(mockMission, strategy);

            // ---Then---
            assertThat(score).isZero();
        }
    }
}