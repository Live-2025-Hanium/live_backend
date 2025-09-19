package com.example.live_backend.domain.mission.clover.entity;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("VISIT")
@NoArgsConstructor
@Getter
public class VisitMission extends CloverMission {

    @Column(name = "target_place_category")
    private String targetPlaceCategory;

    public VisitMission(String targetPlaceCategory) {
        this.targetPlaceCategory = targetPlaceCategory;
    }

    @Override
    public CloverType getCloverType() {
        return CloverType.VISIT;
    }
}
