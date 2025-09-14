package com.example.live_backend.domain.place.service;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.place.dto.ActiveMissionPlace;
import com.example.live_backend.domain.place.dto.Location;
import com.example.live_backend.domain.place.entity.PlaceVisitMission;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.domain.place.repository.PlaceVisitMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceMissionService {
    
    private final PlaceVisitMissionRepository placeVisitMissionRepository;
    private final MemberRepository memberRepository;
    
    public Optional<ActiveMissionPlace> getActiveMissionPlace(String oauthId) {
        log.info("Getting active mission place for user: {}", oauthId);
        
        Member member = memberRepository.findByOauthId(oauthId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + oauthId));
        
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