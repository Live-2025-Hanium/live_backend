package com.example.live_backend.domain.place.dto;

import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.global.page.Pagination;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "장소 검색 결과")
public class PlaceSearchResult {
    
    private final PageTemplate<PlaceItem> pageData;
    
    private PlaceSearchResult(PageTemplate<PlaceItem> pageData) {
        this.pageData = pageData;
    }
    
    // PageTemplate의 필드들에 대한 delegate 메서드들
    public long getTotalElements() {
        return pageData.totalElements();
    }
    
    public int getTotalPages() {
        return pageData.totalPages();
    }
    
    public int getCurrentPage() {
        return pageData.currentPage();
    }
    
    public int getPageSize() {
        return pageData.pageSize();
    }
    
    public boolean isHasNext() {
        return pageData.hasNext();
    }
    
    public List<PlaceItem> getContent() {
        return pageData.content();
    }
    
    public static PlaceSearchResult from(PageTemplate<PlaceItem> pageTemplate) {
        return new PlaceSearchResult(pageTemplate);
    }
    
    public static PlaceSearchResult of(List<PlaceItem> content, Pagination pagination) {
        PageTemplate<PlaceItem> pageTemplate = PageTemplate.of(content, pagination);
        return new PlaceSearchResult(pageTemplate);
    }
    
    public static PlaceSearchResult simple(List<PlaceItem> items, int page, int size, boolean hasNext) {
        // 카카오 API는 전체 개수를 제공하지 않으므로 간단한 페이지 정보만 제공
        long estimatedTotal = hasNext ? (long) (page + 1) * size + 1 : (long) page * size;
        int totalPages = hasNext ? page + 1 : page;
        
        // Pagination 객체 생성
        Pagination pagination = Pagination.of(estimatedTotal, page, size);
        
        // PageTemplate 생성
        PageTemplate<PlaceItem> pageTemplate = new PageTemplate<>(
            estimatedTotal,
            totalPages,
            page,
            size,
            hasNext,
            items != null ? items : List.of()
        );
        
        return new PlaceSearchResult(pageTemplate);
    }
}