package com.zerototen.savegame.member.domain.controller;

import com.zerototen.savegame.member.domain.OAuth.GoogleOAuth;
import com.zerototen.savegame.member.domain.controller.dto.MemberDto;
import com.zerototen.savegame.member.domain.controller.dto.MemberDto.socialLoginResponse;
import com.zerototen.savegame.member.domain.service.OAuthService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class GoogleController {
  private final GoogleOAuth googleoauth;
  private final OAuthService oAuthService;

  // 구글 로그인 창 접근
  @GetMapping("google")
  public void getGoogleAuthUrl(HttpServletResponse response) throws Exception {
    response.sendRedirect(googleoauth.getOauthRedirectURL());
  }

  // 구글 로그인 이후
  @GetMapping("google/login/redirect")
  public ResponseEntity<socialLoginResponse> callback(
      @RequestParam(name = "code") String code) throws IOException {
    return oAuthService.googleLogin(code);
  }
}
