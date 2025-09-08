package com.example.live_backend.domain.mission.clover.entity;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TIMER")
@NoArgsConstructor
@Getter
public class TimerMission extends CloverMission {

    @Column(name = "required_seconds")
    private int requiredSeconds;

    public TimerMission(int requiredSeconds) {
        this.requiredSeconds = requiredSeconds;
    }

    @Override
    public CloverType getCloverType() {
        return CloverType.TIMER;
    }
}
