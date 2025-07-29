package com.example.live_backend.global.storage.dto;

import com.example.live_backend.global.storage.UploadType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequestDto {

    @Schema(description = "업로드할 파일명", example = "profile_image.jpg")
    private String fileName;
    
    @Schema(description = "파일의 Content-Type", example = "image/jpeg")
    private String contentType;

    @Schema(description = "업로드 타입", example = "PROFILE")
    private UploadType uploadType;
}

