package com.example.live_backend.domain.mission.my.dto;

import com.example.live_backend.domain.mission.my.Enum.RepeatType;
import com.example.live_backend.domain.mission.my.Enum.MyMissionStatus;
import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MyMissionRecordResponseDto {

    private Long userMissionId;
    private String missionTitle;
    private MyMissionStatus myMissionStatus;
    private String scheduledTime;
    private RepeatType repeatType;

    public static MyMissionRecordResponseDto from(MyMissionRecord myMissionRecord) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = myMissionRecord.getMyMission().getScheduledTime() != null
                ? myMissionRecord.getMyMission().getScheduledTime().format(formatter)
                : null;

        return MyMissionRecordResponseDto.builder()
                .userMissionId(myMissionRecord.getId())
                .missionTitle(myMissionRecord.getMyMission().getTitle())
                .myMissionStatus(myMissionRecord.getMyMissionStatus())
                .scheduledTime(formattedTime)
                .repeatType(myMissionRecord.getMyMission().getRepeatType())
                .build();
    }
}