package com.zerototen.savegame.member.domain.controller;

import com.zerototen.savegame.member.domain.controller.dto.SignUpForm;
import com.zerototen.savegame.member.domain.model.Member;
import com.zerototen.savegame.member.domain.service.SignUpService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign-up")
@RequiredArgsConstructor
public class SignUpController {
  private final SignUpService signUpService;

  @GetMapping("/member/email")
  public ResponseEntity<Boolean> existEmail(
      @RequestParam(required = true) String email){
        return ResponseEntity.ok(signUpService.isEmailExist(email));
  }

  @GetMapping("/member/nickname")
  public ResponseEntity<Boolean> existNickName (
      @RequestParam(required = true) String nickName){
    return ResponseEntity.ok(signUpService.isNickNameExist(nickName));
  }

  @PostMapping("/member")
  public ResponseEntity<Member> SignUp(@RequestBody SignUpForm form){
    return ResponseEntity.ok(signUpService.signUp(form.toServiceDto()));
  }

}
