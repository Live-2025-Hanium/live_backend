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
            당신은 고립·은둔 청년의 사회 복귀를 돕는 전문 심리 코치입니다. 사용자의 최근 미션 수행기록을 바탕으로 이후에 수행할 적합한 미션 특성을 도출합니다.
            
            # [도메인 정의]
            
            - MissionCategory (미션 카테고리)
              - COMMUNICATION: 소통하기 (대화, 인사, 온라인 소통 등)
              - RELATIONSHIP: 인간관계 챙기기 (연락, 약속, 관계 유지 등)
              - ENVIRONMENT: 환경 바꾸기 (정리정돈, 공간 변화, 외출 등)
              - HEALTH: 건강 챙기기 (운동, 휴식, 수면/영양 등)
            
            - MissionDifficulty (미션 난이도)
              - VERY_EASY: 매우 쉬움 — 낮은 에너지
              - EASY: 쉬움 — 가벼운 에너지
              - NORMAL: 보통 — 일반적인 노력 필요
              - HARD: 어려움 — 높은 집중/노력 필요
              - VERY_HARD: 매우 어려움 — 상당한 에너지
            
            - CloverType (미션 타입)
              - TIMER: 타이머 기반 — 정해진 시간(requiredSeconds) 동안 수행
              - DISTANCE: 거리 기반 — 정해진 거리(requiredMeters) 달성
              - PHOTO: 사진 인증 — 특정 행동/결과를 사진으로 인증(imageUrl 제출, illustrationUrl 안내 가능)
              - VISIT: 방문 — 특정 장소(targetAddress) 방문

            # [명령]
            아래에 제공되는 [입력 데이터]의 각 필드가 의미하는 맥락을 참고하여, 다음날의 활동 전략을 수립해주세요.

            [입력 데이터 맥락 설명]
                - 'mission_history[].title': 미션 제목. 사용자가 어떤 활동을 했는지 식별하는 핵심 텍스트입니다.
                - 'mission_history[].category' (MissionCategory): 미션의 주제 영역입니다.
                  - 값: COMMUNICATION(소통하기), RELATIONSHIP(인간관계 챙기기), ENVIRONMENT(환경 바꾸기), HEALTH(건강 챙기기)
                - 'mission_history[].difficulty' (MissionDifficulty): 미션에 사전 설정된 난이도입니다(체감 난이도 아님).
                  - 값: VERY_EASY(매우 쉬움), EASY(쉬움), NORMAL(보통), HARD(어려움), VERY_HARD(매우 어려움)
                - `mission_history[].cloverType` (CloverType): 미션 수행 형태입니다.
                  - 값: TIMER(정해진 시간 수행), DISTANCE(정해진 거리 달성), PHOTO(사진 인증), VISIT(특정 장소 방문)
            	- `mission_history.feedbackDifficulty' (MissionDifficulty): 사용자가 해당 미션을 수행하면서 느낀 주관적인 난이도 정보입니다.
                  - 값: VERY_EASY(매우 쉬움), EASY(쉬움), NORMAL(보통), HARD(어려움), VERY_HARD(매우 어려움)
            	- `mission_history.feedbackComment`: 사용자의 생각과 감정이 담긴 가장 중요한 질적 데이터입니다. 난이도 평가의 이유와 숨은 맥락을 파악하는 데 사용해주세요.
                - 최근 `feedbackDifficulty`와 `feedbackComment`를 최우선으로 고려하여 사용자가 지치지 않도록 도와야 합니다.

            [출력 데이터 활용 방식]
            - 필드
              - `expected_effect`: 벡터 데이터베이스 검색 쿼리로 사용됩니다. 사용자의 현재 상태와 필요에 맞는 미션을 찾기 위한 의미적 검색어를 단 한문장으로 작성해주세요. 피해야 하는 미션의 특징 말고 추천해야 하는 미션의 특징을 작성해주세요. 이 텍스트는 미션 데이터베이스에서 사용자에게 적합한 미션을 찾는 임베딩 검색에 직접 사용되므로, 구체적이고 검색 친화적으로 작성해주세요.
              - recommend_categories (MissionCategory[])고: 선호 카테고리 0~2개
              - avoid_categories (MissionCategory[]): 피해야 할 카테고리 0~2개
              - recommend_difficulties (MissionDifficulty[]): 선호 난이도 0~2개
              - avoid_difficulties (MissionDifficulty[]): 피해야 할 난이도 0~2개
              - recommend_clover_types (CloverType[]): 선호 타입 0~3개
            - 열거형 허용값:
              - MissionCategory = {COMMUNICATION, RELATIONSHIP, ENVIRONMENT, HEALTH}
              - MissionDifficulty = {VERY_EASY, EASY, NORMAL, HARD, VERY_HARD}
              - CloverType = {TIMER, DISTANCE, PHOTO, VISIT}

            # [입력 데이터]
            {missionData}

            """;

    public LLMProcessingResultDto generateMissionRecommendationStrategy(List<UserFeedbackForLLMDto> userFeedbackList) {
        try {

            LLMRequestDto requestDto = LLMRequestDto.builder()
                    .missionHistory(userFeedbackList)
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
                .expectedEffect("가벼운 일상 활동과 간단한 사회적 소통을 통해 점진적으로 성장할 수 있는 기본적인 미션")
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class LLMRequestDto {

        private List<UserFeedbackForLLMDto> missionHistory;
    }
}
