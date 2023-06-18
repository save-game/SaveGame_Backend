package com.zerototen.savegame.service;

import static com.zerototen.savegame.exception.ErrorCode.ALREADY_REGISTER_EMAIL;
import static com.zerototen.savegame.exception.ErrorCode.LOGIN_CHECK_FAIL;
import static com.zerototen.savegame.exception.ErrorCode.NOT_CONTAINS_EXCLAMATIONMARK;
import static com.zerototen.savegame.exception.ErrorCode.NOT_EMAIL_FORM;
import static com.zerototen.savegame.exception.ErrorCode.NOT_SOCIAL_LOGIN;
import static com.zerototen.savegame.exception.ErrorCode.PASSWORD_SIZE_ERROR;
import static com.zerototen.savegame.exception.ErrorCode.WANT_SOCIAL_REGISTER;
import com.zerototen.savegame.config.jwt.TokenProvider;
import com.zerototen.savegame.domain.Member;
import com.zerototen.savegame.domain.common.UserVo;
import com.zerototen.savegame.domain.dto.MemberDto;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 회원가입
    @Transactional
    public ResponseEntity<MemberDto.SaveDto> register(MemberDto.SaveDto request) {
        registerValidation(request);
        Member member = memberRepository.save(
            Member.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .imageUrl(request.getUserImage())
                .build()
        );

        String accessToken = tokenProvider.createToken(member.getEmail(), member.getId());
        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());

        return new ResponseEntity<>(MemberDto.SaveDto.response(member, accessToken, refreshToken),
            HttpStatus.OK);
    }

    //로그인
    @Transactional
    public ResponseEntity<MemberDto.LoginDto> login(MemberDto.LoginDto request) {
        loginValidate(request);

        Member member = memberRepository.findByEmail(request.getEmail())
            .orElseThrow(  () -> new CustomException(LOGIN_CHECK_FAIL));

        String accessToken = tokenProvider.createToken(member.getEmail(), member.getId());
        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());

        return new ResponseEntity<>(MemberDto.LoginDto.response(accessToken, refreshToken),
            HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> delete(MemberDto.DeleteDto request){
        Member deletedMember = memberRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(LOGIN_CHECK_FAIL));

        if(!passwordEncoder.matches(request.getPassword(), deletedMember.getPassword())){
            throw new CustomException(LOGIN_CHECK_FAIL);
        }
        deletedMember.setDeletedAt(LocalDateTime.now());

            return new ResponseEntity<String>("삭제가 완료되었습니다.", HttpStatus.OK);
    }

    public UserVo LoginToken(String token){
        return tokenProvider.getUserVo(token);
    }

    // 로그인 시 메일 및 비밀번호 검증
    private void loginValidate(MemberDto.LoginDto request) {
        Member member = memberRepository.findByEmail(request.getEmail())
            .orElseThrow(
                () -> new CustomException(LOGIN_CHECK_FAIL)
            );

        if (member.getDeletedAt() != null){
            throw new CustomException(LOGIN_CHECK_FAIL);
        }

        if (request.getEmail().contains("gmail")) {
            throw new CustomException(NOT_SOCIAL_LOGIN);
        }

        if (!passwordEncoder.matches(request.getPassword(),
            memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(LOGIN_CHECK_FAIL)
                ).getPassword())
        ) {
            throw new CustomException(LOGIN_CHECK_FAIL);
        }
    }

    // 회원 가입 시 검증
    private void registerValidation(MemberDto.SaveDto request) {
        if (request.getEmail().contains("gmail")) {
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

    public boolean isEmailExist(String email) {
        return memberRepository.findByEmail(email.toLowerCase(Locale.ROOT))
            .isPresent();
    }

    public boolean isNicknameExist(String nickname) {
        return memberRepository.findByNickname(nickname.toLowerCase(Locale.ROOT))
            .isPresent();
    }

    public Optional<Member> findByIdAndEmail(Long id, String email){
        return memberRepository.findById(id)
            .stream().filter(customer->customer.getEmail().equals(email))
            .findFirst();
    }
}
