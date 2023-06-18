package com.zerototen.savegame.controller;

import static com.zerototen.savegame.exception.ErrorCode.NOT_FOUND_USER;

import com.zerototen.savegame.config.jwt.TokenProvider;
import com.zerototen.savegame.domain.Member;
import com.zerototen.savegame.domain.common.UserVo;
import com.zerototen.savegame.domain.dto.MemberDto;
import com.zerototen.savegame.domain.dto.MemberDto.DetailDto;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;


    @GetMapping("/test")
    public String test() {
        return "CONNECTION_SUCCESS";
    }

    @GetMapping("/test/token")
    public ResponseEntity<DetailDto> getInfo(@RequestHeader(
        name = "X-AUTH-TOKEN") String token) {
        UserVo vo = tokenProvider.getUserVo(token);
        Member m = memberService.findByIdAndEmail(vo.getId(), vo.getEmail())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        return ResponseEntity.ok(MemberDto.DetailDto.response(m));
    }
}