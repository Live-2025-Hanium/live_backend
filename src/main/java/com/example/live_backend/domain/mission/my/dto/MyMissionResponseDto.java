package com.example.live_backend.domain.mission.my.dto;

import com.example.live_backend.domain.mission.my.Enum.RepeatType;
import com.example.live_backend.domain.mission.my.entity.MyMission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MyMissionResponseDto {

    private Long myMissionId;
    private String missionTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String scheduledTime;
    private RepeatType repeatType;
    private boolean isActive;

    public static MyMissionResponseDto from(MyMission myMission) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = myMission.getScheduledTime() != null
                ? myMission.getScheduledTime().format(formatter)
                : null;

        return MyMissionResponseDto.builder()
                .myMissionId(myMission.getId())
                .missionTitle(myMission.getTitle())
                .startDate(myMission.getStartDate())
                .endDate(myMission.getEndDate())
                .scheduledTime(formattedTime)
                .repeatType(myMission.getRepeatType())
                .isActive(myMission.isActive())
                .build();
    }
}