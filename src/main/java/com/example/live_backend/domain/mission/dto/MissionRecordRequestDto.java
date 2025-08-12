package com.example.live_backend.domain.mission.dto;

import com.example.live_backend.domain.mission.Enum.MissionDifficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "미션 기록 생성 요청 DTO")
public class MissionRecordRequestDto {

    @Schema(description = "미션 기록 ID", example = "1")
    private Long userMissionId;

    @Schema(description = "피드백 코멘트", example = "미션을 완료하면서 정말 뿌듯했습니다!")
    private String feedbackComment;

    @Schema(description = "체감 난이도", example = "EASY")
    private MissionDifficulty feedbackDifficulty;

    @Schema(description = "인증샷 이미지 URL (PHOTO 미션일 때 필수)", example = "https://s3.amazonaws.com/bucket/mission-certification/image.jpg")
    private String imageUrl;
}