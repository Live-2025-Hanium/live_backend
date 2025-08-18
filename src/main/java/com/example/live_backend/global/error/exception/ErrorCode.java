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
	MEMBER_NICKNAME_REQUIRED(BAD_REQUEST, "닉네임은 필수 입력값입니다."),
	MEMBER_NICKNAME_LENGTH_INVALID(BAD_REQUEST, "닉네임은 2자 이상 20자 이하여야 합니다."),
	MEMBER_NICKNAME_CHARACTER_INVALID(BAD_REQUEST, "닉네임은 한글, 영문, 숫자만 사용 가능합니다."),
	MEMBER_NICKNAME_SPACE_NOT_ALLOWED(BAD_REQUEST, "닉네임에는 공백을 사용할 수 없습니다."),
	MEMBER_BIRTHDATE_INVALID(BAD_REQUEST, "생년월일은 미래일 수 없습니다."),
	INVALID_MISSION_STATUS(BAD_REQUEST, "미션 상태 변경을 할 수 없습니다."),
	INVALID_UPLOAD_TYPE(BAD_REQUEST, "업로드 타입이 일치하지 않습니다."),
	INVALID_CLOVER_TYPE(BAD_REQUEST, "지원하지 않는 클로버 타입입니다."),
	FAILED_TO_CONVERT_TO_JSON(BAD_REQUEST, "JSON로 변환에 실패했습니다."),
	FAILED_TO_CONVERT_FROM_JSON(BAD_REQUEST, "JSON으로부터 변환에 실패했습니다."),
	MISSION_EXPIRED(BAD_REQUEST, "미션 완료할 수 있는 날짜가 아닙니다."),
	INVALID_MISSION_TYPE(BAD_REQUEST, "미션 타입이 일치하지 않습니다."),
	IMAGE_URL_REQUIRED(BAD_REQUEST, "PHOTO 미션에는 인증샷이 필요합니다."),
	INVALID_QUERY_TYPE(BAD_REQUEST, "잘못된 쿼리 타입입니다."),
	QUERY_TYPE_REQUIRED(BAD_REQUEST, "쿼리 타입은 필수 입력값입니다."),

	/* ------------------ 401 UNAUTHORIZED : 인증 관련 오류 ------------------ */
	DENIED_UNAUTHORIZED_USER(UNAUTHORIZED, "로그인되지 않은 유저의 접근입니다."),
	EXPIRED_TOKEN(UNAUTHORIZED, "토큰이 만료되었습니다."),
	INVALID_TOKEN_CATEGORY(UNAUTHORIZED, "유효하지 않은 토큰 유형입니다."),
	INVALID_TOKEN(UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	MISSING_TOKEN(UNAUTHORIZED, "토큰이 없습니다."),
	MISSING_REFRESH_TOKEN(UNAUTHORIZED, "리프레시 토큰이 존재하지 않습니다."),

	/* ------------------ 403 FORBIDDEN : 권한 없음 ------------------ */
	MISSION_FORBIDDEN(FORBIDDEN, "미션을 변경할 권한이 없습니다."),
	REGISTRATION_DENIED(FORBIDDEN, "미션을 등록할 권한이 없습니다."),
	MISSION_UPDATE_DENIED(FORBIDDEN, "미션을 수정할 권한이 없습니다."),
	MISSION_DELETE_DENIED(FORBIDDEN, "미션을 삭제할 권한이 없습니다."),

	/* ------------------ 404 NOT_FOUND: 리소스 없음 ------------------ */
	USER_NOT_FOUND(NOT_FOUND, "존재하지 않는 사용자입니다."),
	EXAMPLE_NOT_FOUND(NOT_FOUND, "존재하지 않는 예제입니다."),
	MISSION_NOT_FOUND(NOT_FOUND, "존재하지 않는 미션입니다."),
	SURVEY_NOT_FOUND(NOT_FOUND, "존재하지 않는 설문입니다."),

	/* ------------------ 500 INTERNAL_SERVER_ERROR : 서버 오류 ------------------ */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	UNSUPPORTED_CLOVER_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 클로버 미션 타입입니다."),
	S3_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 연결에 실패했습니다."),
	PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "업로드 URL 생성에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String detail;
}
