package com.example.live_backend.domain.board.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.board.enums.ReactionType;
import com.example.live_backend.domain.memeber.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_reactions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "member_id", "reaction_type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public BoardReaction(Board board, Member member, ReactionType reactionType) {
        this.board = board;
        this.member = member;
        this.reactionType = reactionType;
        this.deletedAt = null;
    }

    public void activate() {
        this.deletedAt = null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
} 