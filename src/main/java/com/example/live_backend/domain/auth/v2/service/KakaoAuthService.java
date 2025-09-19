package com.example.live_backend.domain.auth.v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.live_backend.infra.kakao.oauth.dto.KakaoTokenResponse;
import com.example.live_backend.infra.kakao.oauth.dto.KakaoUserResponse;
import com.example.live_backend.infra.kakao.oauth.feign.KakaoOAuthFeign;
import com.example.live_backend.infra.kakao.oauth.feign.KakaoUserFeign;
import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthFeign kakaoOAuthFeign;
    private final KakaoUserFeign kakaoUserFeign;

    @Value("${kakao.oauth.client-id}")
    private String clientId;

    @Value("${kakao.oauth.client-secret}")
    private String clientSecret;

    public KakaoLoginRequestDto processOAuthLogin(String code, String redirectUri) {
        try {
            // 1. 인가 코드로 토큰 교환
            KakaoTokenResponse tokenResponse = kakaoOAuthFeign.getToken(
                "authorization_code",
                clientId,
                clientSecret,
                code,
                redirectUri
            );

            // 2. 액세스 토큰으로 사용자 정보 조회
            KakaoUserResponse userResponse = kakaoUserFeign.getUserInfo(
                "Bearer " + tokenResponse.accessToken()
            );

            // 3. KakaoLoginRequestDto로 변환
            return convertToLoginRequest(userResponse);

        } catch (Exception e) {
            log.error("카카오 OAuth 처리 실패: ", e);
            throw new CustomException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    private KakaoLoginRequestDto convertToLoginRequest(KakaoUserResponse response) {
        KakaoLoginRequestDto dto = new KakaoLoginRequestDto();

        // 카카오 ID를 문자열로 변환
        dto.setOauthId(String.valueOf(response.id()));

        // 이메일 (선택 동의 항목일 수 있음)
        if (response.kakaoAccount() != null && response.kakaoAccount().email() != null) {
            dto.setEmail(response.kakaoAccount().email());
        }

        // 닉네임 (프로필 또는 properties에서 가져옴)
        String nickname = null;
        if (response.kakaoAccount() != null &&
            response.kakaoAccount().profile() != null &&
            response.kakaoAccount().profile().nickname() != null) {
            nickname = response.kakaoAccount().profile().nickname();
        } else if (response.properties() != null &&
                   response.properties().nickname() != null) {
            nickname = response.properties().nickname();
        }
        dto.setNickname(nickname);

        // 프로필 이미지 URL
        String profileImageUrl = null;
        if (response.kakaoAccount() != null &&
            response.kakaoAccount().profile() != null &&
            response.kakaoAccount().profile().profileImageUrl() != null) {
            profileImageUrl = response.kakaoAccount().profile().profileImageUrl();
        } else if (response.properties() != null &&
                   response.properties().profileImage() != null) {
            profileImageUrl = response.properties().profileImage();
        }
        dto.setProfileImageUrl(profileImageUrl);

        return dto;
    }
}