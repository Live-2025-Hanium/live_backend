package com.example.live_backend.global.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUrlResponseDto {

    @Schema(description = "파일 업로드를 위한 Presigned URL", example = "https://s3.ap-northeast-2.amazonaws.com/live-hanium/profile-image/1753614019350_b7183036.png")
    private String uploadUrl;

    @Schema(description = "S3에 저장될 파일의 키" , example = "profile-image/1753614019350_b7183036.png")
    private String s3Key;

    @Schema(description = "파일에 접근할 수 있는 URL", example ="https://s3.ap-northeast-2.amazonaws.com/live-hanium/profile-image/1753614019350_b7183036.png")
    private String accessUrl;
}
