package com.example.live_backend.domain.mission.clover.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
public class CloverMissionVectorRepository {

    private static final String MISSION_ID_KEY = "clover_mission_id";

    private final VectorStore vectorStore;

    /**
     * 주어진 텍스트와 가장 유사한 클로버 미션을 검색합니다.
     * @param queryText 유사도 검색을 위한 사용자 상태 요약 텍스트(설문 요약본)
     * @param  count    검색할 미션의 개수
     * @param  excludedMissionIds    검색 제외할 미션의 Id
     * @return 추천 미션 정보가 담긴 DTO 리스트
     */
    public List<Long> searchSimilarMissionsIds(String queryText, int count, List<Long> excludedMissionIds) {

        SearchRequest request = buildSearchRequest(queryText, count, excludedMissionIds);
        List<Document> documents = vectorStore.similaritySearch(request);

        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        return extractMissionIds(documents);
    }

    private SearchRequest buildSearchRequest(String queryText, int count, List<Long> excludedIds) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(queryText)
                .topK(count);

        if (!CollectionUtils.isEmpty(excludedIds)) {
            builder.filterExpression(buildExclusionFilter(excludedIds));
        }

        return builder.build();
    }

    private String buildExclusionFilter(List<Long> excludedIds) {

        return excludedIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(", ", MISSION_ID_KEY + " not in [", "]"));
    }

    private List<Long> extractMissionIds(List<Document> documents) {
        return documents.stream()
                .map(this::extractMissionId)
                .toList();
    }

    private Long extractMissionId(Document document) {
        Object missionId = document.getMetadata().get(MISSION_ID_KEY);
        return Long.valueOf(String.valueOf(missionId));
    }
}
