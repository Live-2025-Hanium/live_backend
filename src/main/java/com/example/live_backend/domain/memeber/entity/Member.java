package com.example.live_backend.domain.memeber.entity;

import com.example.live_backend.domain.memeber.Gender;
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

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private Profile profile;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;

	@Embedded
	private BirthDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Occupation occupation;

	@Column(length = 100)
	private String occupationDetail; // OTHER 선택 시 추가 입력

	@Builder
	public Member(Profile profile,
		Gender gender,
		BirthDate birthDate,
		Occupation occupation,
		String occupationDetail) {
		this.profile = profile;
		this.gender = gender;
		this.birthDate = birthDate;
		this.occupation = occupation;
		this.occupationDetail = occupationDetail;
	}

	public void updateProfile(Profile newProfile) {
		this.profile = newProfile;
	}

}