package com.example.live_backend.domain.mission.clover.dto;

import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloverMissionListResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "오늘의 미션 목록")
    private List<CloverMissionList> missions;

    @Getter
    @Builder
    @Schema(description = "개별 미션 정보")
    public static class CloverMissionList {

        @Schema(description = "미션 기록 ID", example = "10")
        private Long userMissionId;

        @Schema(description = "미션 제목", example = "동료에게 안부 인사하기")
        private String missionTitle;

        @Schema(description = "클로버 미션 타입", example = "TIMER / DISTANCE / PHOTO / VISIT")
        private CloverType cloverType;

        @Schema(description = "미션 수행 상태", example = "ASSIGNED / STARTED / PAUSED / COMPLETED")
        private CloverMissionStatus cloverMissionStatus;

        @Schema(description = "미션 난이도", example = "VERY_EASY / EASY / NORMAL / HARD / VERY_HARD")
        private MissionDifficulty missionDifficulty;

        @Schema(description = "미션 카테고리", example = "RELATIONSHIP / ENVIRONMENT / HEALTH ...")
        private MissionCategory missionCategory;
    }

    /**
     * MissionRecord 리스트를 DTO로 변환하는 정적 팩토리 메서드
     * @param userId         사용자 ID
     * @param missionRecords 변환할 MissionRecord 엔티티 리스트
     * @return 생성된 CloverMissionListResponseDto 객체
     */
    public static CloverMissionListResponseDto of(Long userId, List<CloverMissionRecord> missionRecords) {
        List<CloverMissionList> missionDtoList = missionRecords.stream()
                .map(record -> CloverMissionList.builder()
                        .userMissionId(record.getId())
                        .missionTitle(record.getMissionTitle())
                        .cloverType(record.getCloverType())
                        .cloverMissionStatus(record.getCloverMissionStatus())
                        .missionDifficulty(record.getMissionDifficulty())
                        .missionCategory(record.getMissionCategory())
                        .build())
                .toList();

        return CloverMissionListResponseDto.builder()
                .userId(userId)
                .missions(missionDtoList)
                .build();
    }
}
