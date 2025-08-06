package com.example.live_backend.global.storage;

import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.global.security.PrincipalDetails;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadFacadeTest {

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private UploadFacade uploadFacade;

    private PrincipalDetails userDetails;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .email("test@example.com")
                .oauthId("test-oauth-id")
                .profile(new Profile("testNickname", "test-profile-image-url"))
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        userDetails = new PrincipalDetails(
                member.getId(),
                member.getOauthId(),
                member.getRole().name(),
                member.getProfile().getNickname(),
                member.getEmail()
        );
    }

    @Test
    @DisplayName("성공 - presigned URL 생성 및 반환")
    void generateUrlAndSave_Success() {

        UploadType uploadType = UploadType.PROFILE;
        PresignedUrlRequestDto requestDto = new PresignedUrlRequestDto("profile.jpg", "image/jpeg", uploadType);

        String fakeAccessUrl = "https://test-bucket.s3.amazonaws.com/profile-image/fake-key.jpg";
        PresignedUrlResponseDto expectedResponse = PresignedUrlResponseDto.builder()
                .accessUrl(fakeAccessUrl)
                .build();

        when(s3StorageService.generatePresignedUploadUrl(requestDto)).thenReturn(expectedResponse);

        PresignedUrlResponseDto actualResponse = uploadFacade.generateUrlAndSave(requestDto, userDetails);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.getAccessUrl()).isEqualTo(fakeAccessUrl);
        verify(s3StorageService, times(1)).generatePresignedUploadUrl(requestDto);
    }

    @Test
    @DisplayName("성공 - 다양한 업로드 타입 모두 지원")
    void generateUrlAndSave_AllUploadTypes_Success() {
        PresignedUrlRequestDto profileRequest = new PresignedUrlRequestDto("profile.jpg", "image/jpeg", UploadType.PROFILE);
        PresignedUrlRequestDto postRequest = new PresignedUrlRequestDto("post.jpg", "image/jpeg", UploadType.BOARD);

        String fakeAccessUrl = "https://test-bucket.s3.amazonaws.com/fake-key.jpg";
        PresignedUrlResponseDto expectedResponse = PresignedUrlResponseDto.builder()
                .accessUrl(fakeAccessUrl)
                .build();

        when(s3StorageService.generatePresignedUploadUrl(any())).thenReturn(expectedResponse);

        PresignedUrlResponseDto profileResponse = uploadFacade.generateUrlAndSave(profileRequest, userDetails);
        assertThat(profileResponse.getAccessUrl()).isEqualTo(fakeAccessUrl);

        PresignedUrlResponseDto postResponse = uploadFacade.generateUrlAndSave(postRequest, userDetails);
        assertThat(postResponse.getAccessUrl()).isEqualTo(fakeAccessUrl);

        verify(s3StorageService, times(2)).generatePresignedUploadUrl(any());
    }
}