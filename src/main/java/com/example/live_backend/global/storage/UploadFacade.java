package com.example.live_backend.global.storage;

import com.example.live_backend.domain.memeber.service.MemberService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadFacade {

    private final S3StorageService s3StorageService;
    private final MemberService memberService;
    /*
    * 다른 서비스 추가 예정
    **/

    public PresignedUrlResponseDto generateUrlAndSave(PresignedUrlRequestDto request, PrincipalDetails userDetails) {

        PresignedUrlResponseDto response = s3StorageService.generatePresignedUploadUrl(request);

        UploadType uploadType = request.getUploadType();
        String imageUrl = response.getAccessUrl();
        Long memberId = userDetails.getMemberId();

        switch (uploadType) {
            case PROFILE:
                memberService.updateProfileImage(memberId, imageUrl);
                break;
            /*
            case POST:
                break;
                */
            default:
                throw new CustomException(ErrorCode.INVALID_UPLOAD_TYPE);
        }

        return response;
    }
}
