package com.example.live_backend.domain.mission.my.dto;

import com.example.live_backend.domain.mission.my.Enum.RepeatType;
import com.example.live_backend.domain.mission.my.Enum.MyMissionStatus;
import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MyMissionRecordResponseDto {

    private Long userMissionId;
    private String missionTitle;
    private MyMissionStatus myMissionStatus;
    private List<String> scheduledTime;
    private RepeatType repeatType;

    public static MyMissionRecordResponseDto from(MyMissionRecord myMissionRecord) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<String> formattedTimes = myMissionRecord.getMyMission().getScheduledTime().stream()
                .map(time -> time.format(formatter))
                .toList();

        return MyMissionRecordResponseDto.builder()
                .userMissionId(myMissionRecord.getId())
                .missionTitle(myMissionRecord.getMyMission().getTitle())
                .myMissionStatus(myMissionRecord.getMyMissionStatus())
                .scheduledTime(formattedTimes)
                .repeatType(myMissionRecord.getMyMission().getRepeatType())
                .build();
    }
}