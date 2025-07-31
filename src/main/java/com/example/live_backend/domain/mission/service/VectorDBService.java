package com.example.live_backend.domain.mission.service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class VectorDBService {

    private final VectorStore vectorStore;

    /**
     * 주어진 텍스트와 가장 유사한 클로버 미션을 검색합니다.
     * @param queryText 유사도 검색을 위한 사용자 상태 요약 텍스트(설문 요약본)
     * @param  count    검색할 미션의 개수
     * @return 추천 미션 정보가 담긴 DTO 리스트
     */
    public List<Long> searchSimilarMissionsIds(String queryText, int count) {

        SearchRequest request = SearchRequest.builder()
                .query(queryText)
                .topK(count)
                .build();

        List<Document> recommendedDocuments = vectorStore.similaritySearch(request);

        if (recommendedDocuments == null || recommendedDocuments.isEmpty()) {
            throw new CustomException(ErrorCode.MISSION_NOT_FOUND);
        }

        return recommendedDocuments.stream()
                .map(doc -> Long.valueOf(String.valueOf(doc.getMetadata().get("clover_mission_id"))))
                .toList();
    }
}
