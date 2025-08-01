package com.example.live_backend.domain.memeber.entity.vo;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Profile 엔티티 테스트")
class ProfileTest {

    @Nested
    @DisplayName("닉네임 검증 테스트")
    class NicknameValidationTest {

        @Nested
        @DisplayName("정상 케이스")
        class ValidNicknameTest {

            @ParameterizedTest
            @ValueSource(strings = {
                "홍길동",
                "JohnDoe",
                "User123",
                "사용자123",
                "김ABC",
                "테스트User1",
                "ab",
                "가나다라마바사아자차카타파하AB1234"
            })
            @DisplayName("유효한 닉네임으로 Profile 생성 성공")
            void shouldCreateProfileWithValidNickname(String validNickname) {
                // When & Then
                assertThatNoException().isThrownBy(() -> 
                    Profile.builder()
                        .nickname(validNickname)
                        .profileImageUrl("test-url")
                        .build()
                );
            }

            @Test
            @DisplayName("대소문자 구분하여 서로 다른 닉네임으로 인식")
            void shouldTreatUpperAndLowerCaseAsDifferent() {
                // When
                Profile profile1 = Profile.builder()
                    .nickname("JohnDoe")
                    .profileImageUrl("test-url")
                    .build();
                
                Profile profile2 = Profile.builder()
                    .nickname("johndoe")
                    .profileImageUrl("test-url")
                    .build();

                // Then
                assertThat(profile1.getNickname()).isNotEqualTo(profile2.getNickname());
            }
        }

        @Nested
        @DisplayName("필수값 검증")
        class RequiredValidationTest {

            @Test
            @DisplayName("null 닉네임 - MEMBER_NICKNAME_REQUIRED 예외")
            void shouldThrowExceptionWhenNicknameIsNull() {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname(null)
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_REQUIRED);
            }

            @Test
            @DisplayName("빈 문자열 닉네임 - MEMBER_NICKNAME_REQUIRED 예외")
            void shouldThrowExceptionWhenNicknameIsEmpty() {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname("")
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_REQUIRED);
            }

            @Test
            @DisplayName("공백만 있는 닉네임 - MEMBER_NICKNAME_REQUIRED 예외")
            void shouldThrowExceptionWhenNicknameIsBlank() {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname("   ")
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_REQUIRED);
            }
        }

        @Nested
        @DisplayName("공백 검증")
        class SpaceValidationTest {

            @ParameterizedTest
            @ValueSource(strings = {
                "홍 길동",
                " 홍길동",
                "홍길동 ",
                "홍 길 동",
                "John Doe",
                "사용자 123"
            })
            @DisplayName("공백 포함 닉네임 - MEMBER_NICKNAME_SPACE_NOT_ALLOWED 예외")
            void shouldThrowExceptionWhenNicknameContainsSpace(String nicknameWithSpace) {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname(nicknameWithSpace)
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_SPACE_NOT_ALLOWED);
            }
        }

        @Nested
        @DisplayName("길이 검증")
        class LengthValidationTest {

            @Test
            @DisplayName("1자 닉네임 - MEMBER_NICKNAME_LENGTH_INVALID 예외")
            void shouldThrowExceptionWhenNicknameTooShort() {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname("a")
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_LENGTH_INVALID);
            }

            @Test
            @DisplayName("21자 닉네임 - MEMBER_NICKNAME_LENGTH_INVALID 예외")
            void shouldThrowExceptionWhenNicknameTooLong() {
                // When & Then
                String longNickname = "가나다라마바사아자차카타파하AB12345"; // 21자
                
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname(longNickname)
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_LENGTH_INVALID);
            }
        }

        @Nested
        @DisplayName("문자 구성 검증")
        class CharacterValidationTest {

            @ParameterizedTest
            @ValueSource(strings = {
                "user@name",
                "사용자#123",
                "test$user",
                "nick%name",
                "user&test",
                "nick?name",
                "user!name",
                "nick~name",

            })
            @DisplayName("특수문자 포함 닉네임 - MEMBER_NICKNAME_CHARACTER_INVALID 예외")
            void shouldThrowExceptionWhenNicknameContainsSpecialCharacters(String nicknameWithSpecial) {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname(nicknameWithSpecial)
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "테스트😊",
                "사용자🎉",
                "닉네임❤️",
            })
            @DisplayName("이모지 포함 닉네임 - MEMBER_NICKNAME_CHARACTER_INVALID 예외")
            void shouldThrowExceptionWhenNicknameContainsEmoji(String nicknameWithEmoji) {
                // When & Then
                assertThatThrownBy(() -> 
                    Profile.builder()
                        .nickname(nicknameWithEmoji)
                        .profileImageUrl("test-url")
                        .build()
                ).isInstanceOf(CustomException.class)
                 .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID);
            }
        }
    }

    @Nested
    @DisplayName("Profile 업데이트 테스트")
    class ProfileUpdateTest {

        @Test
        @DisplayName("유효한 닉네임으로 Profile 업데이트 성공")
        void shouldUpdateProfileWithValidNickname() {
            // Given
            Profile originalProfile = Profile.builder()
                .nickname("원래닉네임")
                .profileImageUrl("original-url")
                .build();

            // When
            Profile updatedProfile = originalProfile.update("새로운닉네임123", "new-url");

            // Then
            assertThat(updatedProfile.getNickname()).isEqualTo("새로운닉네임123");
            assertThat(updatedProfile.getProfileImageUrl()).isEqualTo("new-url");
        }

        @Test
        @DisplayName("잘못된 닉네임으로 Profile 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenUpdatingWithInvalidNickname() {
            // Given
            Profile originalProfile = Profile.builder()
                .nickname("원래닉네임")
                .profileImageUrl("original-url")
                .build();

            // When & Then
            assertThatThrownBy(() -> 
                originalProfile.update("잘못된@닉네임", "new-url")
            ).isInstanceOf(CustomException.class)
             .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NICKNAME_CHARACTER_INVALID);
        }
    }
} 