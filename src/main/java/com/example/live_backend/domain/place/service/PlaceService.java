package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.place.dto.*;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.global.page.PageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
    
    private final PlaceSearchService searchService;
    private final PlaceSuggestService suggestService;
    private final PlaceDetailService detailService;
    

    public PageTemplate<PlaceItem> searchByKeyword(SearchRequest request) {
        return searchService.searchByKeyword(request);
    }
    

    public SuggestResponse suggest(SuggestRequest request) {
        return suggestService.suggest(request);
    }
    

    public PageTemplate<PlaceItem> searchByCategory(NearbyRequest request) {
        return searchService.searchByCategory(request);
    }

    public PlaceDetail getPlaceDetail(String placeId) {
        return detailService.getPlaceDetail(placeId);
    }
}