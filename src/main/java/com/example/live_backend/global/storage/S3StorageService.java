package com.example.live_backend.global.storage;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private static final Duration PRESIGN_URL_EXPIRATION  = Duration.ofMinutes(3);

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    /**
     * Presigned URL 을 생성하는 메서드
     * @param request (PresignedUrlRequestDto)
     * @return PresignedUrlResponseDto
     */
    public PresignedUrlResponseDto generatePresignedUploadUrl(PresignedUrlRequestDto request) {
        String s3Key = generateS3Key(request.getFileName(), request.getUploadType());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(request.getContentType())
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(PRESIGN_URL_EXPIRATION)
                    .putObjectRequest(putRequest)
                    .build();

            String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
            String accessUrl = baseUrl + "/" + s3Key;

            return PresignedUrlResponseDto.builder()
                    .uploadUrl(uploadUrl)
                    .s3Key(s3Key)
                    .accessUrl(accessUrl)
                    .build();
        } catch (SdkException e) {
            throw new CustomException(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    private String generateS3Key(String fileName, UploadType uploadType) {

        String directory = uploadType.getDir();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        return directory + "/" + timestamp + "_" + uuid + extension;
    }
}
