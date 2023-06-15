package com.zerototen.savegame.controller;



import com.zerototen.savegame.dto.MemberDto;
import com.zerototen.savegame.dto.MemberDto.SaveDto;
import com.zerototen.savegame.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("app")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 로그인
    @PostMapping("auth/login")
    public ResponseEntity<MemberDto.LoginDto> login(@RequestBody MemberDto.LoginDto request) {
        return memberService.login(request);
    }

    // 회원가입
    @PostMapping("sign-up")
    public ResponseEntity<SaveDto> register(@RequestBody MemberDto.SaveDto request) {
        return memberService.register(request);
    }

    @GetMapping("/exist-email")
    public ResponseEntity<?> existEmail(@RequestParam(required = true) String email) {
        return ResponseEntity.ok(memberService.isEmailExist(email));
    }

    @GetMapping("/exist-nickname")
    public ResponseEntity<?> existNickname(@RequestParam(required = true) String nickname) {
        return ResponseEntity.ok(memberService.isNicknameExist(nickname));
    }

}
