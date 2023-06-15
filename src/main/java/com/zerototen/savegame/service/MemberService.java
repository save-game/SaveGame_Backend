package com.zerototen.savegame.service;

import static com.zerototen.savegame.exception.ErrorCode.ALREADY_REGISTER_EMAIL;
import static com.zerototen.savegame.exception.ErrorCode.LOGIN_CHECK_FAIL;
import static com.zerototen.savegame.exception.ErrorCode.NOT_CONTAINS_EXCLAMATIONMARK;
import static com.zerototen.savegame.exception.ErrorCode.NOT_EMAIL_FORM;
import static com.zerototen.savegame.exception.ErrorCode.NOT_SOCIAL_LOGIN;
import static com.zerototen.savegame.exception.ErrorCode.PASSWORD_SIZE_ERROR;
import static com.zerototen.savegame.exception.ErrorCode.WANT_SOCIAL_REGISTER;
import com.zerototen.savegame.config.jwt.TokenProvider;
import com.zerototen.savegame.domain.dto.MemberDto;
import com.zerototen.savegame.domain.Authority;
import com.zerototen.savegame.domain.Member;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.repository.MemberRepository;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;


    private static Set<Authority> getAuthorities() {
        Authority authority = Authority.builder()
            .authorityName("ROLE_MEMBER")
            .build();
        return Collections.singleton(authority);
    }

    // Service
    // 회원가입
    @Transactional
    public ResponseEntity<MemberDto.SaveDto> register(MemberDto.SaveDto request) {
        REGISTER_VALIDATION(request);
        Member member = memberRepository.save(
            Member.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .imageUrl(request.getUserImage())
                .authorities(getAuthorities())
                .build()
        );

        Authentication authentication = getAuthentication(request.getEmail(),
            request.getPassword());
        String accessToken = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());

        return new ResponseEntity<>(MemberDto.SaveDto.response(member, accessToken, refreshToken),
            HttpStatus.OK);
    }

    //로그인
    @Transactional
    public ResponseEntity<MemberDto.LoginDto> login(MemberDto.LoginDto request) {
        LOGIN_VALIDATE(request);

        Authentication authentication = getAuthentication(request.getEmail(),
            request.getPassword());
        String accessToken = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());

        return new ResponseEntity<>(MemberDto.LoginDto.response(accessToken, refreshToken),
            HttpStatus.OK);
    }


    private void LOGIN_VALIDATE(MemberDto.LoginDto request) {
        memberRepository.findByEmail(request.getEmail())
            .orElseThrow(
                () -> new CustomException(LOGIN_CHECK_FAIL)
            );

        if (request.getEmail().contains("gmail")) {
            throw new CustomException(NOT_SOCIAL_LOGIN);
        }

        //카카오 비활성화
//    if (request.getEmail().contains("daum"))
//      throw new CustomException(NOT_SOCIAL_LOGIN);

        if (!passwordEncoder.matches(
            request.getPassword(),
            memberRepository.findByEmail(request.getEmail())
                .orElseThrow(
                    () -> new CustomException(LOGIN_CHECK_FAIL)
                ).getPassword())
        ) {
            throw new CustomException(LOGIN_CHECK_FAIL);
        }
    }

    private void REGISTER_VALIDATION(MemberDto.SaveDto request) {
/*        if (request.getEmail() == null || request.getPw() == null || request.getName() == null
                || request.getWeight() == null || request.getHeight() == null)
            throw new CustomException(REGISTER_INFO_NULL);*/
        if (request.getEmail().contains("gmail") || request.getEmail().contains("daum")) {
            throw new CustomException(WANT_SOCIAL_REGISTER);
        }

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ALREADY_REGISTER_EMAIL);
        }

        if (!request.getEmail().contains("@")) {
            throw new CustomException(NOT_EMAIL_FORM);
        }

        if (!(request.getPassword().length() > 5)) {
            throw new CustomException(PASSWORD_SIZE_ERROR);
        }

        if (!(request.getPassword().contains("!") || request.getPassword().contains("@")
            || request.getPassword().contains("#")
            || request.getPassword().contains("$") || request.getPassword().contains("%")
            || request.getPassword().contains("^")
            || request.getPassword().contains("&") || request.getPassword().contains("*")
            || request.getPassword().contains("(")
            || request.getPassword().contains(")"))
        ) {
            throw new CustomException(NOT_CONTAINS_EXCLAMATIONMARK);
        }
    }

    private Authentication getAuthentication(String request, String request1) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(request, request1);
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public boolean isEmailExist(String email) {
        return memberRepository.findByEmail(email.toLowerCase(Locale.ROOT))
            .isPresent();
    }

    public boolean isNicknameExist(String nickname) {
        return memberRepository.findByNickname(nickname.toLowerCase(Locale.ROOT))
            .isPresent();
    }

}
