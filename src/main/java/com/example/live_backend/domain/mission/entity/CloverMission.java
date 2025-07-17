package com.example.live_backend.domain.mission.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.mission.Enum.MissionCategory;
import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "missions_clover")
@NoArgsConstructor
@Getter
public class CloverMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionDifficulty difficulty;

    @Builder
    public CloverMission(String title, String description, MissionCategory category, MissionDifficulty difficulty) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
    }
}
