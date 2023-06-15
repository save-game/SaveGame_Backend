package com.zerototen.savegame.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    //회원가입
    WANT_SOCIAL_REGISTER(HttpStatus.BAD_REQUEST, "해당 이메일은 소셜로그인으로 진행해야 합니다."),
    NOT_EMAIL_FORM(HttpStatus.BAD_REQUEST, "이메일 형식이 아닙니다."),
    PASSWORD_SIZE_ERROR(HttpStatus.BAD_REQUEST, "비밀번호가 6자리 이상이여야 합니다."),
    NOT_CONTAINS_EXCLAMATIONMARK(HttpStatus.BAD_REQUEST, "비밀번호에 특수문자가 포함되어있지 않습니다."),

    ALREADY_REGISTER_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용되는 닉네임입니다."),
    ALREADY_REGISTER_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용되는 이메일입니다."),
    NOT_FOUND_USER(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다"),

    //로그인 & 로그아웃 검증
    JWT_TIMEOUT(HttpStatus.BAD_REQUEST, "만료된 JWT 토큰입니다."),
    LOGIN_CHECK_FAIL(HttpStatus.BAD_REQUEST, "아이디나 패스워드를 확인해주세요."),
    NOT_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "소셜 로그인 기능을 이용하세요"),

    // record
    NOT_FOUND_RECORD(HttpStatus.BAD_REQUEST, "지출 내역을 찾을 수 없습니다"),
    NOT_MATCH_MEMBER(HttpStatus.BAD_REQUEST, "해당 내역이 사용자의 내역이 아닙니다"),
    STARTDATE_AFTER_ENDDATE(HttpStatus.BAD_REQUEST, "조회시작일이 조회종료일 이후입니다"),
    CATEGORY_IS_NULL(HttpStatus.BAD_REQUEST, "카테고리가 null 입니다"),
    INVALID_TOTAL(HttpStatus.BAD_REQUEST, "합계가 0 이하 또는 null 입니다");


    private final HttpStatus httpStatus;
    private final String detail;

}