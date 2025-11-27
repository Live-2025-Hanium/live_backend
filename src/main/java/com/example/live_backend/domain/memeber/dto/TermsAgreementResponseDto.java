package com.example.live_backend.domain.memeber.dto;

import com.example.live_backend.domain.memeber.entity.vo.TermsAgreement;
import lombok.Getter;

@Getter
public class TermsAgreementResponseDto {

    private boolean serviceTermsAgreed;
    private boolean privacyPolicyAgreed;
    private boolean locationTermsAgreed;

    public TermsAgreementResponseDto(boolean serviceTermsAgreed,
                                     boolean privacyPolicyAgreed,
                                     boolean locationTermsAgreed) {
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.locationTermsAgreed = locationTermsAgreed;
    }

    public static TermsAgreementResponseDto from(TermsAgreement termsAgreement) {
        return new TermsAgreementResponseDto(
                termsAgreement.isServiceTermsAgreed(),
                termsAgreement.isPrivacyPolicyAgreed(),
                termsAgreement.isLocationTermsAgreed()
        );
    }
}