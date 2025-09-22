package com.example.live_backend.global.storage.controller.docs;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Storage", description = "파일 스토리지 API")
public interface StorageControllerDocs {

    @Operation(summary = "Presigned URL 발급", description = "파일 업로드를 위한 Presigned URL를 발급합니다.")
    ResponseHandler<PresignedUrlResponseDto> generatePresignedUrl(
            @RequestBody PresignedUrlRequestDto request
    );
}
