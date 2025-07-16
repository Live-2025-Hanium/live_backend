package com.example.live_backend.domain.mission.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.example.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_default_id")
    private MissionDefault missionDefault;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // 연관관계 매핑
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MissionRecord> records = new ArrayList<>();

    @Builder
    public MissionUser(User user, MissionDefault missionDefault, String title, String description, LocalDateTime scheduledAt) {
        this.user = user;
        this.missionDefault = missionDefault;
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
    }
}
