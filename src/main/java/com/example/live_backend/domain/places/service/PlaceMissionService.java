package com.example.live_backend.domain.places.service;

import com.example.live_backend.domain.member.entity.Member;
import com.example.live_backend.domain.member.repository.MemberRepository;
import com.example.live_backend.domain.places.dto.ActiveMissionPlace;
import com.example.live_backend.domain.places.dto.Location;
import com.example.live_backend.domain.places.entity.PlaceVisitMission;
import com.example.live_backend.domain.places.repository.PlaceVisitMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceMissionService {
    
    private final PlaceVisitMissionRepository placeVisitMissionRepository;
    private final MemberRepository memberRepository;
    
    public Optional<ActiveMissionPlace> getActiveMissionPlace(String userEmail) {
        log.info("Getting active mission place for user: {}", userEmail);
        
        Member member = memberRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        Optional<PlaceVisitMission> activeMission = placeVisitMissionRepository
            .findActiveMissionByMemberId(member.getId());
        
        return activeMission.map(mission -> ActiveMissionPlace.builder()
            .placeId("kakao:" + mission.getKakaoPlaceId())
            .location(Location.builder()
                .lat(mission.getLatitude())
                .lng(mission.getLongitude())
                .build())
            .name(mission.getPlaceName())
            .build());
    }
}