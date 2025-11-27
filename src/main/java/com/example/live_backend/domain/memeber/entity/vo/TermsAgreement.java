package com.example.live_backend.domain.memeber.entity.vo;

import com.example.live_backend.domain.memeber.dto.TermsAgreementRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement {

    @Column(name = "service_terms_agreed", nullable = false)
    private boolean serviceTermsAgreed;

    @Column(name = "privacy_policy_agreed", nullable = false)
    private boolean privacyPolicyAgreed;

    @Column(name = "location_terms_agreed", nullable = false)
    private boolean locationTermsAgreed;

    public TermsAgreement(boolean serviceTermsAgreed, boolean privacyPolicyAgreed, boolean locationTermsAgreed) {
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.locationTermsAgreed = locationTermsAgreed;
    }

    public static TermsAgreement from(TermsAgreementRequestDto dto) {
        return new TermsAgreement(
                dto.getServiceTermsAgreed(),
                dto.getPrivacyPolicyAgreed(),
                dto.getLocationTermsAgreed()
        );
    }}