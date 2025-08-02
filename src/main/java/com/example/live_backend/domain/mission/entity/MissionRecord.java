package com.example.live_backend.domain.mission.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.Enum.*;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_records")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "mission_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MissionType missionType; // CLOVER, MY

    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "mission_title", nullable = false, length = 50)
    private String missionTitle;

    @Column
    private String missionDescription;

    @Column(name = "clover_type")
    @Enumerated(EnumType.STRING)
    private CloverType cloverType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_category", nullable = false)
    private MissionCategory missionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_difficulty", nullable = false)
    private MissionDifficulty missionDifficulty;

    @Column(name = "required_meters")
    private Integer requiredMeters;

    @Column(name = "progress_in_meters")
    private Integer progressInMeters;

    @Column(name = "required_seconds")
    private Integer requiredSeconds;

    @Column(name = "progress_in_seconds")
    private Integer progressInSeconds;

    @Column(name = "target_address")
    private String targetAddress;

    @Column(name = "illustration_url")
    private String illustrationUrl;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus missionStatus;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Lob
    @Column(name = "feedback_comment")
    private String feedbackComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_difficulty")
    private MissionDifficulty feedbackDifficulty;

    /**
     * CloverMission 엔티티로부터 MissionRecord 엔티티를 생성하는 팩토리 메서드
     * @param cloverMission 원본 클로버 미션
     * @param member 미션을 할당받는 사용자
     * @return 생성된 MissionRecord
     */
    public static MissionRecord fromCloverMission(CloverMission cloverMission, Member member) {
        MissionRecordBuilder builder = MissionRecord.builder()
                .member(member)
                .missionType(MissionType.CLOVER)
                .missionId(cloverMission.getId())
                .missionTitle(cloverMission.getTitle())
                .missionDescription(cloverMission.getDescription())
                .missionCategory(cloverMission.getCategory())
                .missionDifficulty(cloverMission.getDifficulty())
                .missionStatus(MissionStatus.ASSIGNED) // 클로버 미션 기록을 만들었다는 것은 클로버 미션이 할당되었다는 것
                .assignedDate(LocalDate.now());

        // 클로버 미션 타입에 따라 클로버 서브 타입 정보 저장하고 목표치가 있다면 저장, 초기 진행 상황도 0으로 초기화
        if (cloverMission instanceof DistanceMission distanceMission) {
            builder.requiredMeters(distanceMission.getRequiredMeters())
                    .progressInMeters(0)
                    .cloverType(CloverType.DISTANCE);
        } else if (cloverMission instanceof TimerMission timerMission) {
            builder.requiredSeconds(timerMission.getRequiredSeconds())
                    .progressInSeconds(0)
                    .cloverType(CloverType.TIMER);
        } else if (cloverMission instanceof PhotoMission) {
            builder.cloverType(CloverType.PHOTO);
        } else if (cloverMission instanceof VisitMission) {
            builder.cloverType(CloverType.VISIT);
        }

        return builder.build();
    }

    /**
     * 거리 기반 미션의 진행 상황을 업데이트합니다.
     * @param progressInMeters 현재까지 완료한 총 거리 (미터)
     */
    public void updateDistanceProgress(int progressInMeters) {
        if (this.requiredMeters == null) {
            return;
        }

        this.progressInMeters = Math.min(this.requiredMeters, progressInMeters);
    }

    /**
     * 시간 기반 미션의 진행 상황을 업데이트합니다.
     * @param progressInSeconds 현재까지 완료한 총 시간 (초)
     */
    public void updateTimerProgress(int progressInSeconds) {
        if (this.requiredSeconds == null) {
            return;
        }

            this.progressInSeconds = Math.min(this.requiredSeconds, progressInSeconds);
    }

    /**
     * 미션을 시작 상태로 변경합니다.
     * ASSIGNED 또는 PAUSED 상태일 때만 시작할 수 있습니다.
     */
    public void startMission() {
        if (this.missionStatus != MissionStatus.ASSIGNED && this.missionStatus != MissionStatus.PAUSED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.missionStatus = MissionStatus.STARTED;
    }

    /**
     * 미션을 중지 상태로 변경합니다.
     * STARTED 상태일 때만 중지할 수 있습니다.
     */
    public void pauseMission() {
        if (this.missionStatus != MissionStatus.STARTED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.missionStatus = MissionStatus.PAUSED;
    }

    /**
     * 미션을 완료 상태로 변경합니다.
     * STARTED 상태일 때만 완료할 수 있습니다.
     */
    public void completeMission() {
        if (this.missionStatus != MissionStatus.STARTED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.missionStatus = MissionStatus.COMPLETED;
    }
}
