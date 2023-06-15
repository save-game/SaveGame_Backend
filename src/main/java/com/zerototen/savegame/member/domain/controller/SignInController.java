package com.zerototen.savegame.member.domain.controller;

import com.zerototen.savegame.member.domain.application.SignInApplication;
import com.zerototen.savegame.member.domain.controller.dto.SignInForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign-in")
@RequiredArgsConstructor
public class SignInController {

    private final SignInApplication signInApplication;

    @PostMapping("/member")
    public ResponseEntity<String> signInUser(@RequestBody SignInForm form) {
        return ResponseEntity.ok(signInApplication.userloginToken(form
            .toServiceDto()));
    }
}
