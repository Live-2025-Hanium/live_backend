package com.example.live_backend.domain.memeber.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TermsAgreementRequestDto {

    @NotNull(message = "서비스 이용 약관 동의 여부는 필수입니다")
    private Boolean serviceTermsAgreed;

    @NotNull(message = "개인정보 수집 동의 여부는 필수입니다")
    private Boolean privacyPolicyAgreed;

    @NotNull(message = "위치정보 이용 약관 동의 여부는 필수입니다")
    private Boolean locationTermsAgreed;
}