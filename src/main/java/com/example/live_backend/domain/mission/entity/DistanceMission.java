package com.example.live_backend.domain.mission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DISTANCE")
@NoArgsConstructor
@Getter
public class DistanceMission extends CloverMission {

    @Column(name = "required_meters")
    private int requiredMeters;

    public DistanceMission(int requiredMeters) {
        this.requiredMeters = requiredMeters;
    }
}
