package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.MemberService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 회원정보 조회
    @GetMapping("/detail")
    public ResponseDto<?> getDetail(HttpServletRequest request) {
        return memberService.getDetail(request);
    }

    // 비밀번호 수정
    @PutMapping("/detail/password")
    public ResponseDto<?> updatePassword(HttpServletRequest request,
        @RequestBody @Valid UpdatePasswordRequest passwordRequest) {
        return memberService.updatePassword(request, passwordRequest);
    }

    // 닉네임 수정
    @PutMapping("/detail/nickname")
    public ResponseDto<?> updateNickname(HttpServletRequest request,
        @RequestBody @Valid UpdateNicknameRequest nicknameRequest) {
        return memberService.updateNickname(request, nicknameRequest);
    }

    // 프로필 이미지 수정
    @PutMapping("/detail/image")
    public ResponseDto<?> updateProfileImageUrl(HttpServletRequest request,
        @RequestBody UpdateProfileImageUrlRequest imageUrlRequest) {
        return memberService.updateProfileImageUrl(request, imageUrlRequest);
    }

}