package com.zerototen.savegame.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    //회원가입
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용되는 이메일입니다."),
    ALREADY_REGISTERED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용되는 닉네임입니다."),
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다"),

    //로그인 & 로그아웃
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "틀린 비밀번호입니다."),

    // record
    NOT_FOUND_RECORD(HttpStatus.BAD_REQUEST, "지출 내역을 찾을 수 없습니다"),
    NOT_FOUND_POST(HttpStatus.BAD_REQUEST, "게시물을 찾을 수 없습니다"),
    NOT_MATCH_MEMBER(HttpStatus.BAD_REQUEST, "해당 내역이 사용자의 내역이 아닙니다"),
    STARTDATE_AFTER_ENDDATE(HttpStatus.BAD_REQUEST, "조회시작일이 조회종료일 이후입니다"),
    CATEGORY_IS_NULL(HttpStatus.BAD_REQUEST, "카테고리가 null 입니다"),
    INVALID_TOTAL(HttpStatus.BAD_REQUEST, "합계가 0 이하 또는 null 입니다");

    private final HttpStatus httpStatus;
    private final String detail;

}