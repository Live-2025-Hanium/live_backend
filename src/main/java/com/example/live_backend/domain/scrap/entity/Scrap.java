package com.example.live_backend.domain.scrap.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.memeber.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scraps",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "board_id"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Builder
    public Scrap(Member member, Board board) {
        this.member = member;
        this.board = board;
    }
}