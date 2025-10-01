package com.example.live_backend.domain.clover.entity;

import com.example.live_backend.domain.memeber.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clovers")
public class Clover {

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "count", nullable = false)
    private int count = 0;

    public Clover(Member member) {
        this.member = member;
    }

    public void increase(int amount) {
        this.count += amount;
    }
}
