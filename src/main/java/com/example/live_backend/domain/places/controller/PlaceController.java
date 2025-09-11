package com.example.live_backend.domain.places.controller;

import com.example.live_backend.domain.places.docs.PlacesApiDocs;
import com.example.live_backend.domain.places.dto.ActiveMissionPlace;
import com.example.live_backend.domain.places.dto.PlaceDetail;
import com.example.live_backend.domain.places.dto.PlaceSearchResult;
import com.example.live_backend.domain.places.dto.request.NearbyRequest;
import com.example.live_backend.domain.places.dto.request.SearchRequest;
import com.example.live_backend.domain.places.service.PlaceMissionService;
import com.example.live_backend.domain.places.service.PlaceService;
import com.example.live_backend.global.error.response.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ResponseHandler<PlaceSearchResult>> searchByKeyword(
            @Valid @ModelAttribute SearchRequest request) {
        log.info("Search places by keyword: {}", request.getQuery());
        PlaceSearchResult result = placeService.searchByKeyword(request);
        return ResponseEntity.ok(ResponseHandler.success(result));
    }
    
    @Override
    public ResponseEntity<ResponseHandler<PlaceSearchResult>> searchByCategory(
            @Valid @ModelAttribute NearbyRequest request) {
        log.info("Search nearby places by category: {}", request.getCategory());
        PlaceSearchResult result = placeService.searchByCategory(request);
        return ResponseEntity.ok(ResponseHandler.success(result));
    }
    
    @Override
    public ResponseEntity<ResponseHandler<PlaceDetail>> getPlaceDetail(
            @PathVariable String placeId) {
        log.info("Get place detail for: {}", placeId);
        PlaceDetail detail = placeService.getPlaceDetail(placeId);
        return ResponseEntity.ok(ResponseHandler.success(detail));
    }
    
    @Override
    public ResponseEntity<?> getActiveMissionPlace(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get active mission place for user: {}", userDetails.getUsername());
        
        Optional<ActiveMissionPlace> activeMission = placeMissionService
            .getActiveMissionPlace(userDetails.getUsername());
        
        if (activeMission.isPresent()) {
            return ResponseEntity.ok(ResponseHandler.success(activeMission.get()));
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}