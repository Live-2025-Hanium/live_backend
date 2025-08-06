package com.example.live_backend.domain.memeber.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

@Embeddable
@Getter
@NoArgsConstructor
public class BirthDate {

	@Column(name = "birth_date")
	private LocalDate value;

	private BirthDate(LocalDate date) {
		if (date.isAfter(LocalDate.now())) {
			throw new CustomException(ErrorCode.MEMBER_BIRTHDATE_INVALID);
		}
		this.value = date;
	}

	public static BirthDate of(int year, int month, int day) {
		return new BirthDate(LocalDate.of(year, month, day));
	}
}