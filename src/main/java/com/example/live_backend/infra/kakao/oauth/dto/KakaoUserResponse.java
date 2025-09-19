package com.example.live_backend.infra.kakao.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
    @JsonProperty("id")
    Long id,

    @JsonProperty("connected_at")
    String connectedAt,

    @JsonProperty("properties")
    Properties properties,

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {
    public record Properties(
        @JsonProperty("nickname")
        String nickname,

        @JsonProperty("profile_image")
        String profileImage,

        @JsonProperty("thumbnail_image")
        String thumbnailImage
    ) {}

    public record KakaoAccount(
        @JsonProperty("profile_nickname_needs_agreement")
        Boolean profileNicknameNeedsAgreement,

        @JsonProperty("profile_image_needs_agreement")
        Boolean profileImageNeedsAgreement,

        @JsonProperty("profile")
        Profile profile,

        @JsonProperty("has_email")
        Boolean hasEmail,

        @JsonProperty("email_needs_agreement")
        Boolean emailNeedsAgreement,

        @JsonProperty("is_email_valid")
        Boolean isEmailValid,

        @JsonProperty("is_email_verified")
        Boolean isEmailVerified,

        @JsonProperty("email")
        String email,

        @JsonProperty("has_age_range")
        Boolean hasAgeRange,

        @JsonProperty("age_range_needs_agreement")
        Boolean ageRangeNeedsAgreement,

        @JsonProperty("age_range")
        String ageRange,

        @JsonProperty("has_gender")
        Boolean hasGender,

        @JsonProperty("gender_needs_agreement")
        Boolean genderNeedsAgreement,

        @JsonProperty("gender")
        String gender
    ) {}

    public record Profile(
        @JsonProperty("nickname")
        String nickname,

        @JsonProperty("thumbnail_image_url")
        String thumbnailImageUrl,

        @JsonProperty("profile_image_url")
        String profileImageUrl,

        @JsonProperty("is_default_image")
        Boolean isDefaultImage
    ) {}
}