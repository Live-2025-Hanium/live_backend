package com.example.live_backend.domain.survey.util;

import com.example.live_backend.domain.survey.dto.response.SurveyPageResponse;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaginationHelper {

    public static void validatePageNumber(int pageNumber, int totalPages) {
        if (pageNumber < 1) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "페이지 번호는 1 이상이어야 합니다");
        }

        if (pageNumber > totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT,
                String.format("페이지 번호가 범위를 초과했습니다. 최대 페이지: %d", totalPages));
        }
    }

    public static SurveyPageResponse createEmptyResponse(int questionsPerPage) {
        return SurveyPageResponse.of(
            List.of(),
            1,
            0,
            0,
            questionsPerPage
        );
    }

    public static int calculateTotalPages(int totalItems, int itemsPerPage) {
        return (int) Math.ceil((double) totalItems / itemsPerPage);
    }

    public static int calculateStartIndex(int pageNumber, int itemsPerPage) {
        return (pageNumber - 1) * itemsPerPage;
    }

    public static int calculateEndIndex(int startIndex, int itemsPerPage, int totalItems) {
        return Math.min(startIndex + itemsPerPage, totalItems);
    }

    public static List<SurveyQuestionDto> getPageSubList(List<SurveyQuestionDto> allItems,
                                                          int startIndex,
                                                          int endIndex) {
        return allItems.subList(startIndex, endIndex);
    }
}