package com.zerototen.savegame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zerototen.savegame.domain.dto.request.DuplicationRequest;
import com.zerototen.savegame.domain.dto.request.LoginRequest;
import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.security.TokenProvider;
import com.zerototen.savegame.service.KakaoOauthService;
import com.zerototen.savegame.service.MemberService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
//@Controller  // thymeleaf 사용할 경우 @RestController 대신 사용
public class MemberController {

    private final KakaoOauthService kakaoOauthService;
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    private static final String ACCESS_TOKEN_HEADER_NAME = "Authorization";

    @GetMapping("/index")
    public String index() {
        return "loginForm";
    }

    // 회원가입
    @PostMapping("/api/member/signup")
    public ResponseDto<?> signup(@RequestBody @Valid SignupRequest request) {
        return memberService.signup(request);
    }

    // 로그인
    @PostMapping("/api/member/login")
    public ResponseDto<?> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        return memberService.login(request, response);
    }

    // 로그아웃
    @RequestMapping(value = "/api/member/logout", method = RequestMethod.POST)
    public ResponseDto<?> logout(HttpServletRequest request) {
        return memberService.logout(request);
    }

    // 카카오 로그인
    @GetMapping("/api/member/kakaologin")
    public ResponseDto<?> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response,
        HttpServletRequest request) throws JsonProcessingException {
        return kakaoOauthService.kakaoLogin(code, response, request);
    }

    // 카카오 로그아웃 (연동해제)
    @GetMapping("/api/member/kakaologout")
    public ResponseDto<?> kakaoLogout(@RequestParam("code") String code) throws JsonProcessingException {
        return kakaoOauthService.kakaoLogout(code);
    }

    // 이메일 중복 확인
    @PostMapping("/api/member/checkemail")
    public ResponseDto<?> checkDuplicationemail(@RequestBody @Valid DuplicationRequest requestDto) {
        return memberService.checkEmail(requestDto);
    }

    // 닉네임 중복 확인
    @PostMapping("/api/member/checknickname")
    public ResponseDto<?> checkDuplicationNickname(@RequestBody @Valid DuplicationRequest requestDto) {
        return memberService.checkNickname(requestDto);
    }

    // 토큰 재발급
    @GetMapping("/api/member/reissue")
    public ResponseDto<?> getNewAccessToken(HttpServletRequest request, HttpServletResponse response)
        throws ParseException {
        return memberService.reissue(request, response);
    }

    // 회원정보 조회
    @GetMapping("/api/member/detail")
    public ResponseDto<?> getDetail(@RequestHeader(name = ACCESS_TOKEN_HEADER_NAME) String accessToken) {
        return memberService.getDetail(tokenProvider.getMemberIdByToken(accessToken));
    }

    // 비밀번호 수정
    @PutMapping("/api/member/detail/password")
    public ResponseDto<?> updatePassword(@RequestHeader(name = ACCESS_TOKEN_HEADER_NAME) String accessToken,
        @RequestBody @Valid UpdatePasswordRequest request) {
        return memberService.updatePassword(tokenProvider.getMemberIdByToken(accessToken), request);
    }

    // 닉네임 수정
    @PutMapping("/api/member/detail/nickname")
    public ResponseDto<?> updateNickname(@RequestHeader(name = ACCESS_TOKEN_HEADER_NAME) String accessToken,
        @RequestBody @Valid UpdateNicknameRequest request) {
        return memberService.updateNickname(tokenProvider.getMemberIdByToken(accessToken), request);
    }

    // 회원 이미지 수정
    @PutMapping("/api/member/detail/image")
    public ResponseDto<?> updateProfileImageUrl(@RequestHeader(name = ACCESS_TOKEN_HEADER_NAME) String accessToken,
        @RequestBody UpdateProfileImageUrlRequest request) {
        return memberService.updateProfileImageUrl(tokenProvider.getMemberIdByToken(accessToken), request);
    }

    // 회원 탈퇴
    @DeleteMapping("/api/member/withdrawal")
    public ResponseDto<?> withdrawal(@RequestHeader(name = ACCESS_TOKEN_HEADER_NAME) String accessToken) {
        return memberService.withdrawal(tokenProvider.getMemberIdByToken(accessToken));
    }

}