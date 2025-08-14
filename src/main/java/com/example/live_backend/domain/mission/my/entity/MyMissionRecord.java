package com.example.live_backend.domain.mission.my.entity;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.my.Enum.MyMissionStatus;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
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

    public static MyMissionRecord from(MyMission myMission, Member member) {
        return MyMissionRecord.builder()
                .myMission(myMission)
                .member(member)
                .assignedDate(LocalDate.now())
                .myMissionStatus(MyMissionStatus.ASSIGNED)
                .build();
    }

    public void completeMission() {
        if (this.myMissionStatus != MyMissionStatus.ASSIGNED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.myMissionStatus = MyMissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}


