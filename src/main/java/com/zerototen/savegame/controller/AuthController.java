package com.zerototen.savegame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zerototen.savegame.domain.dto.request.DuplicationRequest;
import com.zerototen.savegame.domain.dto.request.LoginRequest;
import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.AuthService;
import com.zerototen.savegame.service.KakaoOauthService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
//@Controller  // thymeleaf 사용할 경우 @RestController 대신 사용
public class AuthController {

    private final KakaoOauthService kakaoOauthService;
    private final AuthService authService;

    @GetMapping("/index")
    public String index() {
        return "loginForm";
    }

    // 회원가입
    @PostMapping("/auth/signup")
    public ResponseDto<?> signup(@RequestBody @Valid SignupRequest request) {
        return authService.signup(request);
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseDto<String> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseDto<?> logout(HttpServletRequest request) {
        return authService.logout(request);
    }

    // 카카오 로그인
    @GetMapping("/auth/kakaologin")
    public ResponseDto<?> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response,
        HttpServletRequest request) throws JsonProcessingException {
        return kakaoOauthService.kakaoLogin(code, response, request);
    }

    // 카카오 로그아웃 (연동해제)
    @GetMapping("/auth/kakaologout")
    public ResponseDto<?> kakaoLogout(@RequestParam("code") String code) throws JsonProcessingException {
        return kakaoOauthService.kakaoLogout(code);
    }

    // 이메일 중복 확인
    @PostMapping("/auth/checkemail")
    public ResponseDto<String> checkDuplicationemail(@RequestBody @Valid DuplicationRequest requestDto) {
        return authService.checkEmail(requestDto);
    }

    // 닉네임 중복 확인
    @PostMapping("/auth/checknickname")
    public ResponseDto<String> checkDuplicationNickname(@RequestBody @Valid DuplicationRequest requestDto) {
        return authService.checkNickname(requestDto);
    }

    // 토큰 재발급
    @GetMapping("/auth/reissue")
    public ResponseDto<String> getNewAccessToken(HttpServletRequest request, HttpServletResponse response)
        throws ParseException {
        return authService.reissue(request, response);
    }

    // 회원 탈퇴
    @DeleteMapping("/auth/withdrawal")
    public ResponseDto<?> withdrawal(HttpServletRequest request) {
        return authService.withdrawal(request);
    }

}
