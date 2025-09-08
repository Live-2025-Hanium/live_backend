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

    @Column(name = "target_address")
    private String targetAddress;

    public VisitMission(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    @Override
    public CloverType getCloverType() {
        return CloverType.VISIT;
    }
}
