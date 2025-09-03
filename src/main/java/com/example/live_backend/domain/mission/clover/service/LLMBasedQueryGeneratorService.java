package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.dto.LLMProcessingResultDto;
import com.example.live_backend.domain.mission.clover.dto.UserFeedbackForLLMDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LLMBasedQueryGeneratorService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public LLMBasedQueryGeneratorService(ChatClient.Builder chatClient,  ObjectMapper objectMapper) {
        this.chatClient = chatClient.build();
        this.objectMapper = objectMapper;
    }

    private static final String PROMPT = """
            # [역할 부여]
            당신은 고립·은둔 청년의 사회 복귀를 돕는 전문 심리 코치입니다. 사용자의 최근 활동 흐름과 상태를 분석하여, 다음날의 활동 전략을 수립하는 역할을 맡았습니다. 당신의 목표는 사용자의 최근 성공/실패 패턴을 파악하여 무리하지 않고 점진적으로 성장하도록 돕는 것입니다.

            # [명령]
            아래에 제공되는 [입력 데이터]의 각 필드가 의미하는 맥락을 참고하여, 다음날의 활동 전략을 수립해주세요.

            [입력 데이터 맥락 설명]
            	- `mission_history`의 `feedbackDifficulty`: 1~5점 척도. 1점은 '매우 쉬움', 3점은 '적당함', 5점은 '매우 어려움'을 의미합니다. 4점 이상은 심리적, 물리적 에너지 소진 가능성을 나타냅니다.
            	- `mission_history`의 `feedbackComment`: 사용자의 생각과 감정이 담긴 가장 중요한 질적 데이터입니다. 난이도 평가의 이유와 숨은 맥락을 파악하는 데 사용해주세요.
                - 최근 `feedbackDifficulty`와 `feedbackComment`를 최우선으로 고려하여 사용자가 지치지 않도록 도와야 합니다.

            [출력 데이터 활용 방식]
            	- `negative_keywords`: 미션 추천 시 제외해야 할 키워드들입니다. 사용자가 부담스러워하거나 피하고 싶어하는 요소들을 포함해주세요.
            	- `expected_effect`: **벡터 데이터베이스 검색 쿼리로 사용됩니다.** 사용자의 현재 상태와 필요에 맞는 미션을 찾기 위한 의미적 검색어를 단 한문장으로 작성해주세요. 
            	  피해야 하는 미션의 특징 말고 추천해야 하는 미션의 특징을 작성해주세요. 이 텍스트는 미션 데이터베이스에서 사용자에게 적합한 미션을 찾는 임베딩 검색에 직접 사용되므로, 구체적이고 검색 친화적으로 작성해주세요.
            	  
            [expected_effect 작성 가이드]
            	- 사용자의 현재 심리적/감정적 상태를 반영
            	- 추천받고 싶은 미션의 특성이나 방향성을 포함
            	- 난이도 수준과 활동 유형에 대한 힌트 제공
            	- 예시: "가벼운 사회적 활동으로 자신감을 회복하고 싶어하는 상태", "실내에서 할 수 있는 창의적이고 차분한 활동 필요"

            # [입력 데이터]
            {missionData}

            분석 결과를 negative_keywords(피해야 할 요소들 배열)와 expected_effect(벡터DB 검색용 사용자 상태 및 니즈 설명) 형식으로 제공해주세요.
            """;

    public LLMProcessingResultDto generateMissionRecommendationStrategy(List<UserFeedbackForLLMDto> dto) {
        try {
            // DTO 변환
            List<MissionHistoryItem> historyItems = dto.stream()
                    .map(MissionHistoryItem::from)
                    .toList();

            LLMRequestDto requestDto = LLMRequestDto.builder()
                    .missionHistory(historyItems)
                    .build();

            // JSON 변환
            String missionDataJson = objectMapper.writeValueAsString(requestDto);

            // ChatClient를 사용한 구조화된 응답 요청
            LLMProcessingResultDto response = chatClient.prompt()
                    .user(PROMPT.replace("{missionData}", missionDataJson))
                    .call()
                    .entity(LLMProcessingResultDto.class);

            return response;

        } catch (Exception e) {
            return createFallbackResponse();
        }
    }

    private LLMProcessingResultDto createFallbackResponse() {
        return LLMProcessingResultDto.builder()
                .negativeKeywords(List.of())
                .expectedEffect("가벼운 일상 활동과 간단한 사회적 소통을 통해 점진적으로 성장할 수 있는 기본적인 미션")
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class LLMRequestDto {
        @JsonProperty("mission_history")
        private List<MissionHistoryItem> missionHistory;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class MissionHistoryItem {
        private String title;

        @JsonProperty("feedbackDifficulty")
        private Integer feedbackDifficulty;

        @JsonProperty("feedbackComment")
        private String feedbackComment;

        public static MissionHistoryItem from(UserFeedbackForLLMDto dto) {
            return MissionHistoryItem.builder()
                    .title(dto.getMissionTitle())
                    .feedbackDifficulty(convertDifficultyToNumber(dto.getFeedbackDifficulty()))
                    .feedbackComment(dto.getFeedbackComment())
                    .build();
        }

        private static Integer convertDifficultyToNumber(MissionDifficulty difficulty) {
            if (difficulty == null) return 3;
            return switch (difficulty) {
                case VERY_EASY -> 1;
                case EASY -> 2;
                case NORMAL -> 3;
                case HARD -> 4;
                case VERY_HARD -> 5;
            };
        }
    }
}
