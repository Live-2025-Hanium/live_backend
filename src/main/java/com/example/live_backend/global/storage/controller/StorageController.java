package com.example.live_backend.global.storage.controller;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;
import com.example.live_backend.global.storage.StorageService;
import com.example.live_backend.global.storage.controller.docs.StorageControllerDocs;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "파일 스토리지 API")

public class StorageController implements StorageControllerDocs {

    private final StorageService storageService;

    @Override
    @PostMapping("/presigned-url")
    @AuthenticatedApi(reason = "Presigned Url 발급 요청은 로그인한 사용자만 가능합니다.")
    public ResponseHandler<PresignedUrlResponseDto> generatePresignedUrl(
            @RequestBody PresignedUrlRequestDto request
    ) {

        PresignedUrlResponseDto response = storageService.generatePresignedUploadUrl(request);

        return ResponseHandler.success(response);
    }

}

