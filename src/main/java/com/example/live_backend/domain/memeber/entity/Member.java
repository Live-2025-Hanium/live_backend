package com.example.live_backend.domain.memeber.entity;

import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.vo.BirthDate;
import com.example.live_backend.domain.memeber.entity.vo.Profile;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

	@Column(name = "last_survey_submitted_at")
	private LocalDateTime lastSurveySubmittedAt;

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

}