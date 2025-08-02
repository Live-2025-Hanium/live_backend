package com.example.live_backend.domain.mission.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.mission.Enum.CloverType;
import com.example.live_backend.domain.mission.Enum.MissionCategory;
import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "missions_clover")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "mission_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@Getter
public abstract class CloverMission extends BaseEntity {

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

    protected CloverMission(String title, String description, MissionCategory category, MissionDifficulty difficulty) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
    }

    /**
     * AdminRegisterCloverMissionRequestDto 를 받아 적절한 타입의 CloverMission 인스턴스를
     * 생성하고 초기화하는 정적 팩토리 메서드
     */
    public static CloverMission from(AdminRegisterCloverMissionRequestDto dto) {
        CloverMission mission;
        CloverType cloverType = dto.getCloverType();

        if (cloverType.equals(CloverType.DISTANCE)) {
            mission = new DistanceMission(dto.getRequiredMeters());
        } else if (cloverType.equals(CloverType.TIMER)) {
            mission = new TimerMission(dto.getRequiredSeconds());
        } else if (cloverType.equals(CloverType.PHOTO)) {
            mission = new PhotoMission(dto.getIllustrationUrl());
        } else if (cloverType.equals(CloverType.VISIT)) {
            mission = new VisitMission(dto.getTargetAddress());
        } else {
            throw new CustomException(ErrorCode.INVALID_CLOVER_TYPE);
        }

        mission.title = dto.getMissionTitle();
        mission.description = dto.getDescription();
        mission.category = dto.getMissionCategory();
        mission.difficulty = dto.getMissionDifficulty();

        return mission;
    }
}
