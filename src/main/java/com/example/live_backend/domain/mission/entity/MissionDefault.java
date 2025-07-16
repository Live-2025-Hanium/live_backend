package com.example.live_backend.domain.mission.entity;

import com.example.live_backend.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions_default")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MissionDefault extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    @NotBlank
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    // 연관관계 매핑
    @OneToMany(mappedBy = "missionDefault", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MissionUser> userMissions = new ArrayList<>();

    @Getter
    public enum Category {

        RELATIONSHIP("인간관계"),
        ENVIRONMENT("환경 바꾸기");
        /**
         * 앞으로 더 추가 예정
         */

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }
    }

    // 미션 난이도 ENUM
    @Getter
    public enum Difficulty {
        EASY(1),
        MEDIUM(2),
        HARD(3);

        private final int level;

        Difficulty(int level) {
            this.level = level;
        }
    }

    @Builder
    public MissionDefault(String title, String description, Category category, Difficulty difficulty) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
    }
}
