package com.example.live_backend.domain.mission.clover.entity;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PHOTO")
@NoArgsConstructor
@Getter
public class PhotoMission extends CloverMission {

    @Column(name = "illustration_url")
    private String illustrationUrl;

    public PhotoMission(String illustrationUrl) {
        this.illustrationUrl = illustrationUrl;
    }

    @Override
    public CloverType getCloverType() {
        return CloverType.PHOTO;
    }
}
