package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.security.TokenProvider;
import com.zerototen.savegame.service.MemberService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    private static final String ACCESS_TOKEN = "Authorization";

    // 회원정보 조회
    @GetMapping("/detail")
    public ResponseDto<?> getDetail(@RequestHeader(name = ACCESS_TOKEN) String accessToken) {
        return memberService.getDetail(tokenProvider.getMemberIdByToken(accessToken));
    }

    // 비밀번호 수정
    @PutMapping("/detail/password")
    public ResponseDto<?> updatePassword(@RequestHeader(name = ACCESS_TOKEN) String accessToken,
        @RequestBody @Valid UpdatePasswordRequest request) {
        return memberService.updatePassword(tokenProvider.getMemberIdByToken(accessToken), request);
    }

    // 닉네임 수정
    @PutMapping("/detail/nickname")
    public ResponseDto<?> updateNickname(@RequestHeader(name = ACCESS_TOKEN) String accessToken,
        @RequestBody @Valid UpdateNicknameRequest request) {
        return memberService.updateNickname(tokenProvider.getMemberIdByToken(accessToken), request);
    }

    // 프로필 이미지 수정
    @PutMapping("/detail/image")
    public ResponseDto<?> updateProfileImageUrl(@RequestHeader(name = ACCESS_TOKEN) String accessToken,
        @RequestBody UpdateProfileImageUrlRequest request) {
        return memberService.updateProfileImageUrl(tokenProvider.getMemberIdByToken(accessToken), request);
    }

}