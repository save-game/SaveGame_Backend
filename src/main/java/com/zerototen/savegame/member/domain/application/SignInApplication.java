package com.zerototen.savegame.member.domain.application;

import com.zerototen.savegame.config.JwtAuthenticationProvider;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.member.domain.common.UserType;
import com.zerototen.savegame.member.domain.controller.dto.SignInForm;
import com.zerototen.savegame.member.domain.model.Member;
import com.zerototen.savegame.member.domain.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignInApplication {

    private final MemberService memberService;
    private final JwtAuthenticationProvider provider;

    public String userloginToken(SignInForm form) {
        // 1. 로그인 가능 여부
        Member m = memberService.findValidUser(form.getEmail(), form.getPassword())
            .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_CHECK_FAIL));
        // 2. 토큰을 발행하고
        // 3. 토큰을 response한다.
        return provider.createToken(m.getEmail(), m.getId(), UserType.MEMBER);
    }


}
