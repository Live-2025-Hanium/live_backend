package com.example.live_backend.global.error.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum ErrorCode {

	/* ------------------ 400 BAD_REQUEST : 잘못된 요청 ------------------ */
	INVALID_LOGIN_INFO(BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
	INVALID_INPUT(BAD_REQUEST, "잘못된 입력값입니다."),
	VALIDATION_FAILED(BAD_REQUEST, "입력값 검증에 실패했습니다."),

	/* ------------------ 401 UNAUTHORIZED : 인증 관련 오류 ------------------ */
	DENIED_UNAUTHORIZED_USER(UNAUTHORIZED, "로그인되지 않은 유저의 접근입니다."),

	/* ------------------ 404 NOT_FOUND: 리소스 없음 ------------------ */
	USER_NOT_FOUND(NOT_FOUND, "존재하지 않는 사용자입니다."),
	EXAMPLE_NOT_FOUND(NOT_FOUND, "존재하지 않는 예제입니다."),
	MISSION_NOT_FOUND(NOT_FOUND, "존재하지 않는 미션입니다."),
	SURVEY_NOT_FOUND(NOT_FOUND, "존재하지 않는 설문입니다."),

	/* ------------------ 500 INTERNAL_SERVER_ERROR : 서버 오류 ------------------ */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.")
	;

	private final HttpStatus httpStatus;
	private final String detail;
}
