package com.example.live_backend.domain.mission.entity;

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
}
