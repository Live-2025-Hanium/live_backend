package com.example.live_backend.domain.place.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.mission.clover.entity.VisitMission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_visit_mission")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceVisitMission extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "visit_mission_id", nullable = false)
    private VisitMission visitMission;
    
    @Column(name = "kakao_place_id")
    private String kakaoPlaceId;
    
    @Column(name = "place_name")
    private String placeName;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    public static PlaceVisitMission create(VisitMission visitMission, String kakaoPlaceId, 
                                          String placeName, Double latitude, Double longitude) {
        return PlaceVisitMission.builder()
                .visitMission(visitMission)
                .kakaoPlaceId(kakaoPlaceId)
                .placeName(placeName)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}