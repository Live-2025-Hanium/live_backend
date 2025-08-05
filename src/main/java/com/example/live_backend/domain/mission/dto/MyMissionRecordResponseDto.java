package com.example.live_backend.domain.mission.dto;

import com.example.live_backend.domain.mission.Enum.MissionStatus;
import com.example.live_backend.domain.mission.Enum.MissionType;
import com.example.live_backend.domain.mission.entity.MissionRecord;
import com.example.live_backend.domain.mission.entity.MyMission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MyMissionRecordResponseDto {

    private Long userMissionId;
    private MissionType missionType; // MY
    private String missionTitle;
    private MissionStatus missionStatus;
    private List<String> scheduledTime;
    private List<DayOfWeek> repeatDays;

    /**
     * MyMissionRecordResponseDto 를 생성하는 팩토리 메서드
     */
    public static MyMissionRecordResponseDto from(MissionRecord missionRecord, MyMission myMission) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<String> formattedTimes = myMission.getScheduledTime().stream()
                .map(time -> time.format(formatter))
                .toList();

        return MyMissionRecordResponseDto.builder()
                .userMissionId(missionRecord.getId())
                .missionType(missionRecord.getMissionType())
                .missionTitle(missionRecord.getMissionTitle())
                .missionStatus(missionRecord.getMissionStatus())
                .scheduledTime(formattedTimes)
                .repeatDays(myMission.getRepeatDays())
                .build();
    }
}
