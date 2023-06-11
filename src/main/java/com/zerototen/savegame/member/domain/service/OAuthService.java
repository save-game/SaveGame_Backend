package com.zerototen.savegame.member.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zerototen.savegame.config.Jwt.TokenProvider;
import com.zerototen.savegame.config.RedisDao;
import com.zerototen.savegame.member.domain.OAuth.GoogleOAuth;
import com.zerototen.savegame.member.domain.controller.dto.GoogleOAuthTokenDto;
import com.zerototen.savegame.member.domain.controller.dto.GoogleUserInfoDto;
import com.zerototen.savegame.member.domain.controller.dto.MemberDto;
import com.zerototen.savegame.member.domain.controller.dto.MemberDto.socialLoginResponse;
import com.zerototen.savegame.member.domain.model.Authority;
import com.zerototen.savegame.member.domain.model.Member;
import com.zerototen.savegame.member.domain.repository.MemberRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
  private final MemberRepository memberRepository;
  private final GoogleOAuth googleOAuth;
  private final TokenProvider tokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final RedisDao redisDao;
  private final PasswordEncoder passwordEncoder;
  UsernamePasswordAuthenticationToken authenticationToken = null;

  private static Set<Authority> getAuthorities() {
    Authority authority = Authority.builder()
        .authorityName("ROLE_MEMBER")
        .build();
    return Collections.singleton(authority);
  }
  private ResponseEntity<MemberDto.socialLoginResponse> Login(String email, String name, String uimg) {
    if (email.contains("gmail")) {
      authenticationToken = new UsernamePasswordAuthenticationToken(email, "google");
      log.info("gmail!");
    }
    //카카오 로그인 비활성화
//    if (email.contains("daum")) {
//      authenticationToken = new UsernamePasswordAuthenticationToken(email, "kakao");
//      log.info("daum!");
//    }

    Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String atk = tokenProvider.createToken(authentication);
    String rtk = tokenProvider.createRefreshToken(email);

    redisDao.setValues(email, rtk, Duration.ofDays(14));

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Authorization", "Bearer " + atk);

    return new ResponseEntity<>(MemberDto.socialLoginResponse.response(
        name, email, uimg, atk, rtk, "SOCIAL_LOGIN_TRUE"
    ), HttpStatus.OK);
  }

  private GoogleUserInfoDto getGoogleUserInfoDto(String code) throws JsonProcessingException {
    ResponseEntity<String> accessTokenResponse = googleOAuth.requestAccessToken(code);
    GoogleOAuthTokenDto oAuthToken = googleOAuth.getAccessToken(accessTokenResponse);
    ResponseEntity<String> userInfoResponse = googleOAuth.requestUserInfo(oAuthToken);
    GoogleUserInfoDto googleUser = googleOAuth.getUserInfo(userInfoResponse);
    return googleUser;
  }

  // Service
  // 구글 로그인 서비스
  @Transactional
  public ResponseEntity<socialLoginResponse> googleLogin(String code) throws IOException {
    GoogleUserInfoDto googleUser = getGoogleUserInfoDto(code);
    String email = googleUser.getEmail();
    String name = googleUser.getName();
    // 첫 로그인시 사용자 정보를 보내줌
    if (!memberRepository.existsByEmail(email)) {
      memberRepository.save(
          Member.builder()
              .nickname(googleUser.getName())
              .email(googleUser.getEmail())
              .imageUrl(googleUser.getPicture())
              .password(passwordEncoder.encode("google"))
              .authorities(getAuthorities())
              .build()
      );

      return new ResponseEntity<>(MemberDto.socialLoginResponse.response(
          name, email, null, null, null, "SOCIAL_REGISTER_TRUE"
      ), HttpStatus.OK);
    }
    // 이메일이 존재할시 로그인
    return Login(email, name, null);
  }
}
