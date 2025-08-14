package com.example.live_backend.domain.mission.my.entity;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.entity.*;
import com.example.live_backend.domain.mission.my.Enum.MyMissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_mission_records")
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyMissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_mission_id", nullable = false)
    private MyMission myMission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_status", nullable = false)
    private MyMissionStatus myMissionStatus;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static MyMissionRecord from(MyMission myMission) {
        return MyMissionRecord.builder()
                .myMission(myMission)
                .assignedDate(LocalDate.now())
                .myMissionStatus(MyMissionStatus.ASSIGNED)
                .build();
    }
}


