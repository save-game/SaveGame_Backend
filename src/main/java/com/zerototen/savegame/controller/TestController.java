package com.zerototen.savegame.controller;

import com.zerototen.savegame.security.TokenProvider;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TokenProvider tokenProvider;

    @GetMapping("/test")
    public String test() {
        return "CONNECTION_SUCCESS";
    }

    @GetMapping("/test/me")
    public String whoami(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication().getEmail();
    }
}