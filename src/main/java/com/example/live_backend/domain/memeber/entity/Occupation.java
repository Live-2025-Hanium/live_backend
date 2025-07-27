package com.example.live_backend.domain.memeber.entity;

public enum Occupation {
	STUDENT("학생"),
	EMPLOYEE("직장인"),
	HOMEMAKER("주부"),
	FREELANCER("프리랜서/자영업"),
	UNEMPLOYED("무직/구직 중"),
	OTHER("기타(직접 입력)");

	private final String description;

	Occupation(String description) {
		this.description = description;
	}
}