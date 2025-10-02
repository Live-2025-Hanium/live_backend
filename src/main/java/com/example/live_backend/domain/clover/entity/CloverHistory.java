package com.example.live_backend.domain.clover.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.memeber.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clover_history")
public class CloverHistory extends BaseEntity {

    private static final int CLOVER_REWARD_FOR_MISSION_COMPLETION = 1;
    private static final String MISSION_COMPLETION_REASON_FORMAT = "클로버 미션 완료: %s";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false)
    private int amount; // ex) +1, -100 ..

    /**
     * TODO : 현재는 클로버 미션 완료에 따른 이유만 있지만, 추후 여러 사유 추가 예정
     */
    @Column(name = "reason", length = 200)
    private String reason;

    @Builder
    public CloverHistory(Member member, int amount, String reason) {
        this.member = member;
        this.amount = amount;
        this.reason = reason;
    }

    public static CloverHistory fromMissionCompletion(Member member, String missionTitle) {
        return CloverHistory.builder()
                .member(member)
                .amount(CLOVER_REWARD_FOR_MISSION_COMPLETION)
                .reason(String.format(MISSION_COMPLETION_REASON_FORMAT, missionTitle))
                .build();
    }
}
