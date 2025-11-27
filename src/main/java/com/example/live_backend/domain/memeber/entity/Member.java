package com.example.live_backend.domain.memeber.entity;

import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.vo.BirthDate;
import com.example.live_backend.domain.memeber.entity.vo.Profile;

import com.example.live_backend.domain.memeber.entity.vo.TermsAgreement;
import com.example.live_backend.domain.survey.vitality.enums.VitalityLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "oauth_id", unique = true, nullable = false)
	private String oauthId;

	@Column(name = "email", nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Embedded
	private Profile profile;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Embedded
	private BirthDate birthDate;

	@Enumerated(EnumType.STRING)
	private Occupation occupation;

	@Column(length = 100)
	private String occupationDetail; // OTHER 선택 시 추가 입력

	@Enumerated(EnumType.STRING)
	@Column(name = "vitality_level")
	private VitalityLevel vitalityLevel;

	@Column(name = "last_survey_submitted_at")
	private LocalDateTime lastSurveySubmittedAt;

	@Column(name = "clover_count", nullable = false)
	private int cloverCount = 0;

    @Embedded
    private TermsAgreement termsAgreement;

	@Builder
	public Member(String oauthId,
		String email,
		Role role,
		Profile profile,
		Gender gender,
		BirthDate birthDate,
		Occupation occupation,
		String occupationDetail) {
		this.oauthId = oauthId;
		this.email = email;
		this.role = role;
		this.profile = profile;
		this.gender = gender;
		this.birthDate = birthDate;
		this.occupation = occupation;
		this.occupationDetail = occupationDetail;
		this.cloverCount = 0;
        this.termsAgreement = new TermsAgreement(false, false, false);
	}

	public void updateProfile(Profile newProfile) {
		this.profile = newProfile;
	}

	public void updateDetails(Gender gender, BirthDate birthDate, Occupation occupation, String occupationDetail) {
		this.gender = gender;
		this.birthDate = birthDate;
		this.occupation = occupation;
		this.occupationDetail = occupationDetail;
	}

	public void updateLastSurveySubmittedAt(LocalDateTime lastSurveySubmittedAt) {
		this.lastSurveySubmittedAt = lastSurveySubmittedAt;
	}

	public void updateVitalityLevel(VitalityLevel vitalityLevel) {
		this.vitalityLevel = vitalityLevel;
	}

	public void increaseCloverCount(int amount) {
		this.cloverCount += amount;
	}

	public int getCloverCount() {
		return this.cloverCount;
	}

    public void updateTermsAgreement(TermsAgreement termsAgreement) {
        this.termsAgreement = termsAgreement;
    }
}