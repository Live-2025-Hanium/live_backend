package com.example.live_backend.domain.mission.my.entity;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.Enum.RepeatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "missions_my")
public class MyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type")
    private RepeatType repeatType;

    public static MyMission from(MyMissionRequestDto dto, Member member) {

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        RepeatType repeatType = dto.getRepeatType();

        if (startDate == null && endDate == null) {
            startDate = LocalDate.now();
            endDate = LocalDate.now();
            repeatType = null;
        }

        return MyMission.builder()
                .member(member)
                .title(dto.getMissionTitle())
                .isActive(true)
                .startDate(startDate)
                .endDate(endDate)
                .scheduledTime(dto.getScheduledTime())
                .repeatType(repeatType)
                .build();
    }

    public void update(MyMissionRequestDto dto) {
        this.title = dto.getMissionTitle();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();

        if (dto.getScheduledTime() != null) {
            this.scheduledTime = dto.getScheduledTime();
        }

        if (dto.getRepeatType() != null) {
            this.repeatType = dto.getRepeatType();
        }
    }

    public void changeActive(boolean isActive) {
        this.isActive = isActive;
    }
}