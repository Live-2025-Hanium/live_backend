package com.example.live_backend.domain.mission.clover.entity;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.Enum.*;
import com.example.live_backend.domain.mission.entity.*;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clover_mission_records")
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class CloverMissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @Column(name = "mission_title", nullable = false, length = 100)
    private String missionTitle;

    @Column(name = "mission_description", length = 300)
    private String missionDescription;

    @Column(name = "mission_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CloverMissionStatus cloverMissionStatus;

    @Column(name = "clover_type")
    @Enumerated(EnumType.STRING)
    private CloverType cloverType;

    @Column(name = "mission_category")
    @Enumerated(EnumType.STRING)
    private MissionCategory missionCategory;

    @Column(name = "mission_difficulty")
    @Enumerated(EnumType.STRING)
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
    private LocalDate assignedDate; // 2025-06-17

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 2025-06-17T22:46:17.408

    @Column(name = "feedback_comment")
    private String feedbackComment;

    @Column(name = "feedback_difficulty")
    @Enumerated(EnumType.STRING)
    private MissionDifficulty feedbackDifficulty;

    @Column(name = "image_url")
    private String imageUrl;

    public static CloverMissionRecord from(CloverMission cloverMission, Member member) {
        CloverMissionRecord.CloverMissionRecordBuilder builder = CloverMissionRecord.builder()
                .member(member)
                .missionId(cloverMission.getId())
                .missionTitle(cloverMission.getTitle())
                .missionDescription(cloverMission.getDescription())
                .cloverMissionStatus(CloverMissionStatus.ASSIGNED) // 클로버 미션 기록을 만들었다는 것은 클로버 미션이 할당되었다는 것
                .missionCategory(cloverMission.getCategory())
                .missionDifficulty(cloverMission.getDifficulty())
                .assignedDate(LocalDate.now());

        // 클로버 미션 타입에 따라 클로버 서브 타입 정보 저장하고 목표치가 있다면 저장, 초기 진행 상황도 0으로 초기화
        if (cloverMission instanceof DistanceMission distanceMission) {
            builder.cloverType(CloverType.DISTANCE)
                    .requiredMeters(distanceMission.getRequiredMeters())
                    .progressInMeters(0);
        } else if (cloverMission instanceof TimerMission timerMission) {
            builder.cloverType(CloverType.TIMER)
                    .requiredSeconds(timerMission.getRequiredSeconds())
                    .progressInSeconds(0);
        } else if (cloverMission instanceof PhotoMission) {
            builder.cloverType(CloverType.PHOTO);
        } else if (cloverMission instanceof VisitMission) {
            builder.cloverType(CloverType.VISIT);
        }

        return builder.build();
    }

    public void updateDistanceProgress(int progressInMeters) {
        if (this.requiredMeters == null) {
            return;
        }

        this.progressInMeters = Math.min(this.requiredMeters, progressInMeters);
    }

    public void updateTimerProgress(int progressInSeconds) {
        if (this.requiredSeconds == null) {
            return;
        }

        this.progressInSeconds = Math.min(this.requiredSeconds, progressInSeconds);
    }

    public void startMission() {
        if (this.cloverMissionStatus != CloverMissionStatus.ASSIGNED && this.cloverMissionStatus != CloverMissionStatus.PAUSED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.cloverMissionStatus = CloverMissionStatus.STARTED;
    }


    public void pauseMission() {
        if (this.cloverMissionStatus != CloverMissionStatus.STARTED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.cloverMissionStatus = CloverMissionStatus.PAUSED;
    }


    public void completeMission() {
        if (this.cloverMissionStatus != CloverMissionStatus.STARTED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }
        this.cloverMissionStatus = CloverMissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void addFeedback(String feedbackComment, MissionDifficulty feedbackDifficulty) {
        setFeedback(feedbackComment, feedbackDifficulty, null);
    }

    public void addFeedbackWithImage(String feedbackComment, MissionDifficulty feedbackDifficulty, String imageUrl) {
        setFeedback(feedbackComment, feedbackDifficulty, imageUrl);
    }

    public void updateFeedback(String feedbackComment, MissionDifficulty feedbackDifficulty) {
        setFeedback(feedbackComment, feedbackDifficulty, null);
    }
    public void updateFeedbackWithImage(String feedbackComment, MissionDifficulty feedbackDifficulty, String imageUrl) {
        setFeedback(feedbackComment, feedbackDifficulty, imageUrl);
    }

    public void setFeedback(String feedbackComment, MissionDifficulty feedbackDifficulty, String imageUrl) {

        if (this.cloverMissionStatus != CloverMissionStatus.COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_MISSION_STATUS);
        }

        this.feedbackComment = feedbackComment;
        this.feedbackDifficulty = feedbackDifficulty;
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }
}


