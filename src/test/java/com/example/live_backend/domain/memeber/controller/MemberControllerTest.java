package com.example.live_backend.domain.memeber.controller;

import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.domain.memeber.service.MemberService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController 온보딩 테스트")
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_NICKNAME = "테스트유저123";

    @Nested
    @DisplayName("닉네임 중복 확인 로직")
    class NicknameCheckTest {

        @Test
        @DisplayName("성공 - 사용 가능한 닉네임")
        void checkNickname_Available_Success() {
            // Given
            String nickname = "새로운닉네임123";
            NicknameCheckResponseDto response = NicknameCheckResponseDto.available();
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(memberService.checkNicknameAvailability(eq(nickname), eq(TEST_USER_ID)))
                .willReturn(response);

            // When
            ResponseHandler<NicknameCheckResponseDto> result = memberController.checkNickname(nickname, userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().isAvailable()).isTrue();
            assertThat(result.getData().getMessage()).isEqualTo("사용 가능한 닉네임입니다.");
        }

        @Test
        @DisplayName("성공 - 사용 불가능한 닉네임 (중복)")
        void checkNickname_Unavailable_Success() {
            // Given
            String nickname = "중복된닉네임";
            NicknameCheckResponseDto response = NicknameCheckResponseDto.unavailable();
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(memberService.checkNicknameAvailability(eq(nickname), eq(TEST_USER_ID)))
                .willReturn(response);

            // When
            ResponseHandler<NicknameCheckResponseDto> result = memberController.checkNickname(nickname, userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().isAvailable()).isFalse();
            assertThat(result.getData().getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
        }

        @Test
        @DisplayName("실패 - 잘못된 닉네임으로 예외 발생")
        void checkNickname_InvalidNickname_ThrowsException() {
            // Given
            String invalidNickname = "잘못된@닉네임";
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(memberService.checkNicknameAvailability(eq(invalidNickname), eq(TEST_USER_ID)))
                .willThrow(new CustomException(ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID));

            // When & Then
            assertThatThrownBy(() -> memberController.checkNickname(invalidNickname, userDetails))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID);
        }
    }

    @Nested
    @DisplayName("프로필 등록/수정 로직")
    class ProfileRegistrationTest {

        @Test
        @DisplayName("성공 - 프로필 등록")
        void registerProfile_Success() {
            // Given
            MemberProfileRequestDto request = createValidProfileRequest();
            MemberResponseDto response = createMemberResponse();
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(memberService.registerOrUpdateProfile(any(MemberProfileRequestDto.class), eq(TEST_USER_ID)))
                .willReturn(response);

            // When
            ResponseHandler<MemberResponseDto> result = memberController.registerProfile(request, userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getNickname()).isEqualTo("새로운닉네임123");
            assertThat(result.getData().getGender()).isEqualTo("MALE");
            verify(memberService).registerOrUpdateProfile(request, TEST_USER_ID);
        }

        @Test
        @DisplayName("실패 - 잘못된 닉네임으로 예외 발생")
        void registerProfile_InvalidNickname_ThrowsException() {
            // Given
            MemberProfileRequestDto request = createInvalidProfileRequest("잘못된@닉네임");
            PrincipalDetails userDetails = createPrincipalDetails();
            
            given(memberService.registerOrUpdateProfile(any(MemberProfileRequestDto.class), eq(TEST_USER_ID)))
                .willThrow(new CustomException(ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID));

            // When & Then
            assertThatThrownBy(() -> memberController.registerProfile(request, userDetails))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID);
        }
    }

    private PrincipalDetails createPrincipalDetails() {
        return new PrincipalDetails(TEST_USER_ID, "oauth-id", "USER", TEST_NICKNAME, "test@example.com");
    }

    private MemberProfileRequestDto createValidProfileRequest() {
        MemberProfileRequestDto request = new MemberProfileRequestDto();
        setField(request, "nickname", "새로운닉네임123");
        setField(request, "profileImageUrl", "https://s3.amazonaws.com/profile/image.jpg");
        setField(request, "gender", "MALE");
        setField(request, "birthYear", 1995);
        setField(request, "birthMonth", 3);
        setField(request, "birthDay", 15);
        setField(request, "occupation", "STUDENT");
        setField(request, "occupationDetail", null);
        return request;
    }

    private MemberProfileRequestDto createInvalidProfileRequest(String invalidNickname) {
        MemberProfileRequestDto request = createValidProfileRequest();
        setField(request, "nickname", invalidNickname);
        return request;
    }

    private MemberResponseDto createMemberResponse() {
        return new MemberResponseDto(
            TEST_USER_ID,
            "새로운닉네임123",
            "https://s3.amazonaws.com/profile/image.jpg",
            "MALE",
            "1995-03-15",
            "STUDENT",
            null,
            null  // lastSurveySubmittedAt
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
} 