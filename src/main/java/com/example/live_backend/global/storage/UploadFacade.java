package com.example.live_backend.global.storage;

import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadFacade {

    private final S3StorageService s3StorageService;
    /*
    * 다른 서비스 추가 예정
    **/

    public PresignedUrlResponseDto generateUrlAndSave(PresignedUrlRequestDto request, PrincipalDetails userDetails) {

        PresignedUrlResponseDto response = s3StorageService.generatePresignedUploadUrl(request);

        // UploadFacade는 단순히 presigned URL만 제공
        // 실제 DB 업데이트는 각 도메인에서 책임지도록 변경

        return response;
    }
}
