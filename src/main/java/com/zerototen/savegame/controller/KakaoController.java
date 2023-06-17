package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.KakaoAccount;
import com.zerototen.savegame.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class KakaoController {

    private final KakaoService kakaoService;

    // 로그인 후 이 컨트롤러 엔드포인트로 리다이렉팅 시키면, 로그인 과정에서 카카오 인증 서버가 발급해준 code를 들고 이 컨트롤러로 진입하게 된다.
    @GetMapping("/callback")
    public KakaoAccount getKakaoAccount(@RequestParam("code") String code) {
        log.debug("code = {}", code);
        return kakaoService.getInfo(code).getKakaoAccount();
    }

}