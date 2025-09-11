package com.example.live_backend.domain.places.dto;

import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.global.page.Pagination;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장소 검색 결과")
public class PlaceSearchResult extends PageTemplate<PlaceItem> {
    
    public PlaceSearchResult(long totalElements, int totalPages, int currentPage, 
                            int pageSize, boolean hasNext, java.util.List<PlaceItem> content) {
        super(totalElements, totalPages, currentPage, pageSize, hasNext, content);
    }
    
    public static PlaceSearchResult of(java.util.List<PlaceItem> content, Pagination pagination) {
        return new PlaceSearchResult(
            pagination.getTotalElements(),
            pagination.getTotalPages(),
            pagination.getCurrentPage(),
            pagination.getPageSize(),
            pagination.hasNext(),
            content
        );
    }
    
    public static PlaceSearchResult simple(java.util.List<PlaceItem> items, int page, int size, boolean hasNext) {
        // 카카오 API는 전체 개수를 제공하지 않으므로 간단한 페이지 정보만 제공
        long estimatedTotal = hasNext ? (long) (page + 1) * size + 1 : (long) page * size;
        int totalPages = hasNext ? page + 1 : page;
        
        return new PlaceSearchResult(
            estimatedTotal,
            totalPages,
            page,
            size,
            hasNext,
            items
        );
    }
}