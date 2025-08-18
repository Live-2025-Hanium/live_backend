package com.example.live_backend.domain.mission.my.entity;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.util.DayOfWeekListConverter;
import com.example.live_backend.domain.mission.my.util.LocalTimeListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @Convert(converter = LocalTimeListConverter.class)
    @Column(name = "scheduled_time", columnDefinition = "json")
    private List<LocalTime> scheduledTime = new ArrayList<>();

    @Convert(converter = DayOfWeekListConverter.class)
    @Column(name = "repeat_days", columnDefinition = "json")
    private List<DayOfWeek> repeatDays = new ArrayList<>();

    public static MyMission from(MyMissionRequestDto dto, Member member) {
        return MyMission.builder()
                .member(member)
                .title(dto.getMissionTitle())
                .isActive(true)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .scheduledTime(dto.getScheduledTime())
                .repeatDays(dto.getRepeatDays())
                .build();
    }

    public void update(MyMissionRequestDto dto) {
        this.title = dto.getMissionTitle();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();

        if (dto.getScheduledTime() != null) {
            this.scheduledTime.clear();
            this.scheduledTime.addAll(dto.getScheduledTime());
        }

        if (dto.getRepeatDays() != null) {
            this.repeatDays.clear();
            this.repeatDays.addAll(dto.getRepeatDays());
        }
    }
}
