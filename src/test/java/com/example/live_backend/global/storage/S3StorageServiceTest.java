package com.example.live_backend.global.storage;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.storage.dto.PresignedUrlRequestDto;
import com.example.live_backend.global.storage.dto.PresignedUrlResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3StorageService s3StorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3StorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3StorageService, "baseUrl", "https://test-bucket.s3.amazonaws.com");
    }

    @Test
    @DisplayName("성공 - Presigned URL 생성")
    void generatePresignedUploadUrl_Success() throws Exception {

        PresignedUrlRequestDto requestDto = new PresignedUrlRequestDto("profile.jpg", "image/jpeg", UploadType.PROFILE);

        String expectedUploadUrl = "https://test-bucket.s3.amazonaws.com/upload-url?sig=1234";
        URL mockUrl = new URL(expectedUploadUrl);

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUrl);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest);

        PresignedUrlResponseDto responseDto = s3StorageService.generatePresignedUploadUrl(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUploadUrl()).isEqualTo(expectedUploadUrl);
        assertThat(responseDto.getS3Key()).startsWith(UploadType.PROFILE.getDir() + "/");
        assertThat(responseDto.getS3Key()).endsWith(".jpg");
        assertThat(responseDto.getAccessUrl()).isEqualTo("https://test-bucket.s3.amazonaws.com/" + responseDto.getS3Key());
    }

    @Test
    @DisplayName("실패 - Presigned URL 생성")
    void generatePresignedUploadUrl_Failure_When_S3_Throws_Exception() {

        PresignedUrlRequestDto requestDto = new PresignedUrlRequestDto("profile.png", "image/png", UploadType.PROFILE);

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenThrow(SdkException.builder().message("S3 Error").build());

        CustomException exception = assertThrows(CustomException.class, () -> {
            s3StorageService.generatePresignedUploadUrl(requestDto);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
    }
}