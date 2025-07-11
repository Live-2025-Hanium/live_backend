package com.example.live_backend.domain.clover.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.example.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private MissionUser mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // 미션 상태 ENUM
    public enum Status {
        STARTED, COMPLETED, SKIPPED
    }
}
