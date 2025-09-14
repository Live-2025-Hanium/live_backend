package com.example.live_backend.domain.place.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {
	LEISURE("LEI", "AT4", "여가시설"),
	PSYCHIATRY("PSY", "HP8", "정신건강의학과"),
	WELFARE("WEL", "HP8", "복지시설"),
	COUNSELING("CSC", "PO3", "상담센터");

	private final String code;
	private final String kakaoCode;
	private final String label;

	private static final Map<String, PlaceCategory> BY_CODE =
		Arrays.stream(values())
			.collect(Collectors.toMap(PlaceCategory::getCode, e -> e));

	public static PlaceCategory fromCode(String code) {
		return BY_CODE.get(code);
	}

	public static boolean isValidCode(String code) {
		return BY_CODE.containsKey(code);
	}

	public boolean isPsychiatry() {
		return this == PSYCHIATRY;
	}

}