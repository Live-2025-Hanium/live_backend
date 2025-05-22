package com.example.live_backend.common.exception;

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

    /* ------------------ 400 BAD_REQUEST : 인증 관련 오류 ------------------ */
    DENIED_UNAUTHORIZED_USER(UNAUTHORIZED, "로그인되지 않은 유저의 접근입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String detail;
}
