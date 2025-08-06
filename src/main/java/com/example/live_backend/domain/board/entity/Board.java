package com.example.live_backend.domain.board.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.memeber.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 100)
    private String relatedOrganization; // 관련 기관

    @Column(nullable = false)
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardImage> boardImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Board(String title, String content, Category category, String relatedOrganization, Member author) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.relatedOrganization = relatedOrganization;
        this.author = author;
        this.viewCount = 0L;
        this.isDeleted = false;
        this.boardImages = new ArrayList<>();
    }

    public void update(String title, String content, Category category, String relatedOrganization) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.relatedOrganization = relatedOrganization;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void delete() {
        this.isDeleted = true;
    }
} 