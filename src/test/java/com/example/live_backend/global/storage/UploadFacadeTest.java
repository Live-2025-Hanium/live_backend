package com.example.live_backend.global.storage;

import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.service.MemberService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadFacadeTest {

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private UploadFacade uploadFacade;

    private PrincipalDetails userDetails;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .email("test@example.com")
                .oauthId("test-oauth-id")
                .profile(new Profile("test-nickname", "test-profile-image-url"))
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        userDetails = new PrincipalDetails(
                member.getId(),
                member.getOauthId(),
                member.getRole().name(), // Enum의 이름을 String으로 변환
                member.getProfile().getNickname(),
                member.getEmail()
        );
    }

    @Test
    @DisplayName("성공 - 프로필 이미지 업로드 시 MemberService 호출")
    void generateUrlAndSave_Profile_Success() {

        UploadType uploadType = UploadType.PROFILE;
        PresignedUrlRequestDto requestDto = new PresignedUrlRequestDto("profile.jpg", "image/jpeg", uploadType);

        String fakeAccessUrl = "https://test-bucket.s3.amazonaws.com/profile-image/fake-key.jpg";
        PresignedUrlResponseDto s3Response = PresignedUrlResponseDto.builder()
                .accessUrl(fakeAccessUrl)
                .build();

        when(s3StorageService.generatePresignedUploadUrl(requestDto)).thenReturn(s3Response);

        uploadFacade.generateUrlAndSave(requestDto, userDetails);

        verify(memberService, times(1)).updateProfileImage(userDetails.getMemberId(), fakeAccessUrl);
    }

    @Test
    @DisplayName("실패 - 지원하지 않는 업로드 타입 요청 시 예외 발생")
    void generateUrlAndSave_InvalidType_ThrowsException() {
        UploadType unsupportedType = UploadType.POST;
        PresignedUrlRequestDto requestDto = new PresignedUrlRequestDto("test.txt", "text/plain", unsupportedType);

        PresignedUrlResponseDto s3Response = PresignedUrlResponseDto.builder().build();
        when(s3StorageService.generatePresignedUploadUrl(requestDto)).thenReturn(s3Response);

        CustomException exception = assertThrows(CustomException.class, () -> {
            uploadFacade.generateUrlAndSave(requestDto, userDetails);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_UPLOAD_TYPE);
    }
}