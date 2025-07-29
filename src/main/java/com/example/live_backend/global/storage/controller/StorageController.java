package com.example.live_backend.global.storage.controller;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.storage.UploadFacade;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "파일 스토리지 API")

public class StorageController {

    private final UploadFacade uploadFacade;

    @PostMapping("/presigned-url")
    @Operation(summary = "Presigned URL 발급", description = "파일 업로드를 위한 Presigned URL를 발급합니다.")
    public ResponseHandler<PresignedUrlResponseDto> generatePresignedUrl(
            @RequestBody PresignedUrlRequestDto request,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        PresignedUrlResponseDto response = uploadFacade.generateUrlAndSave(request, userDetails);

        return ResponseHandler.success(response);
    }

}

