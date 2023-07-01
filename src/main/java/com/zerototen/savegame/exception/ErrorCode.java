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
    BLANK_TOKEN_HEADER(HttpStatus.BAD_REQUEST, "헤더에 토큰이 없습니다."),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "토큰이 유효하지 않습니다. 다시 로그인해주세요."),
    NOT_EXIST_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "DB에 해당 멤버의 리프레시 토큰이 없습니다."),
    BLACKLIST_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "블랙리스트에 있는 액세스 토큰입니다."),
    INVALIDATE_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다."),
    INVALIDATE_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 액세스 토큰입니다."),
    MISMATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "요청받은 리프레시 토큰이 DB의 리프레시 토큰과 일치하지 않습니다."),
    NOT_EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "아직 만료되지 않은 액세스 토큰입니다."),
    NOT_FOUND_AUTHENTICATION(HttpStatus.BAD_REQUEST, "인증 정보가 없습니다."),

    // 지출
    NOT_FOUND_RECORD(HttpStatus.BAD_REQUEST, "지출 내역을 찾을 수 없습니다"),
    NOT_FOUND_POST(HttpStatus.BAD_REQUEST, "게시물을 찾을 수 없습니다"),
    NOT_MATCH_MEMBER(HttpStatus.BAD_REQUEST, "해당 내역이 사용자의 내역이 아닙니다"),
    STARTDATE_AFTER_ENDDATE(HttpStatus.BAD_REQUEST, "조회시작일이 조회종료일 이후입니다"),
    CATEGORY_IS_NULL(HttpStatus.BAD_REQUEST, "카테고리가 null 입니다"),
    INVALID_TOTAL(HttpStatus.BAD_REQUEST, "합계가 0 이하 또는 null 입니다"),

    ALREADY_REGISTERED_HEART(HttpStatus.BAD_REQUEST, "이미 좋아요가 등록됐습니다."),
    NOT_FOUND_HEART(HttpStatus.BAD_REQUEST, "등록된 하트가 없습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String detail;

}