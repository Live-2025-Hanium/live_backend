package com.example.live_backend.domain.mission.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.example.entity.User;
import com.example.live_backend.domain.mission.Enum.MissionCategory;
import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.Enum.MissionStatus;
import com.example.live_backend.domain.mission.Enum.MissionType;
import jakarta.persistence.*;
import lombok.*;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_records")
@Getter
@NoArgsConstructor
public class MissionRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false)
    private MissionType missionType;

    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "mission_title", nullable = false, length = 50)
    private String missionTitle;

    @Column
    private String missionDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_category", nullable = false)
    private MissionCategory missionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_difficulty", nullable = false)
    private MissionDifficulty missionDifficulty;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus missionStatus;

    @Column(name = "completed_at")
    private DateTime completedAt;

    @Lob
    @Column(name = "feedback_comment")
    private String feedbackComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_difficulty")
    private MissionDifficulty feedbackDifficulty;
}
