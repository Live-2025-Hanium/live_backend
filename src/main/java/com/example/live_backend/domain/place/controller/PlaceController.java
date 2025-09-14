package com.example.live_backend.domain.place.controller;

import com.example.live_backend.domain.place.controller.docs.PlacesApiDocs;
import com.example.live_backend.domain.place.dto.ActiveMissionPlace;
import com.example.live_backend.domain.place.dto.PlaceDetail;
import com.example.live_backend.domain.place.dto.PlaceItem;
import com.example.live_backend.domain.place.dto.SuggestResponse;
import com.example.live_backend.domain.place.dto.request.NearbyRequest;
import com.example.live_backend.domain.place.dto.request.SearchRequest;
import com.example.live_backend.domain.place.dto.request.SuggestRequest;
import com.example.live_backend.global.page.PageTemplate;
import com.example.live_backend.domain.place.service.PlaceMissionService;
import com.example.live_backend.domain.place.service.PlaceService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import com.example.live_backend.global.security.annotation.PublicApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlaceController implements PlacesApiDocs {
    
    private final PlaceService placeService;
    private final PlaceMissionService placeMissionService;
    
    @Override
    @PublicApi(reason = "자동완성 제안은 로그인 없이 가능합니다")
    public ResponseHandler<SuggestResponse> suggest(
            @Valid @ModelAttribute SuggestRequest request) {
        SuggestResponse response = placeService.suggest(request);
        return ResponseHandler.success(response);
    }
    
    @Override
    @PublicApi(reason = "장소 검색은 로그인 없이 가능합니다")
    public ResponseHandler<PageTemplate<PlaceItem>> searchByKeyword(
            @Valid @ModelAttribute SearchRequest request) {
        PageTemplate<PlaceItem> result = placeService.searchByKeyword(request);
        return ResponseHandler.success(result);
    }
    
    @Override
    @PublicApi(reason = "주변 장소 검색은 로그인 없이 가능합니다")
    public ResponseHandler<PageTemplate<PlaceItem>> searchByCategory(
            @Valid @ModelAttribute NearbyRequest request) {
        PageTemplate<PlaceItem> result = placeService.searchByCategory(request);
        return ResponseHandler.success(result);
    }
    
    @Override
    @PublicApi(reason = "장소 상세"
		+ " 조회는 로그인 없이 가능합니다")
    public ResponseHandler<PlaceDetail> getPlaceDetail(
            @PathVariable String placeId) {
        PlaceDetail detail = placeService.getPlaceDetail(placeId);
        return ResponseHandler.success(detail);
    }
    
    @Override
    @AuthenticatedApi(reason = "활성 미션 조회는 로그인한 사용자만 가능합니다")
    public ResponseHandler<ActiveMissionPlace> getActiveMissionPlace(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Optional<ActiveMissionPlace> activeMission = placeMissionService
            .getActiveMissionPlace(userDetails.getUsername());
        
        return ResponseHandler.success(activeMission.orElse(null));
    }
}