package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.TokenDto;
import com.zerototen.savegame.domain.dto.request.DuplicationRequest;
import com.zerototen.savegame.domain.dto.request.LoginRequest;
import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.RefreshToken;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.security.TokenProvider;
import com.zerototen.savegame.util.PasswordUtil;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    // 회원가입
    @Transactional
    public ResponseDto<?> signup(SignupRequest request) {
        Member member = getMemberByEmail(request.getEmail());
        if (member != null) {
            return ResponseDto.fail(ErrorCode.ALREADY_REGISTERED_EMAIL.getDetail());
        }

        if (memberRepository.findByNickname(request.getNickname()).isPresent()) {
            return ResponseDto.fail("중복된 닉네임 입니다.");
        }

        member = Member.builder()
            .email(request.getEmail())
            .password(new BCryptPasswordEncoder().encode(request.getPassword()))
            .nickname(request.getNickname())
            .userRole(Authority.ROLE_MEMBER)
            .build();

        return ResponseDto.success(memberRepository.save(member));
    }

    private Member getMemberByEmail(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        return optionalMember.orElse(null);
    }

    // 로그인
    @Transactional
    public ResponseDto<String> login(LoginRequest request, HttpServletResponse response) {
        Member member = getMemberByEmail(request.getEmail());
        if (member == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_MEMBER.getDetail());
        }

        // 비밀번호 확인
        if (!PasswordUtil.checkPassword(request.getPassword(), member.getPassword())) {
            return ResponseDto.fail(ErrorCode.WRONG_PASSWORD.getDetail());
        }

        if (tokenProvider.isPresentRefreshToken(member) != null) {
            return ResponseDto.fail("이미 로그인되어 있습니다.");
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success("Login Success");
    }

    // 헤더에 토큰담기
    private void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
    }

    // 로그아웃
    @Transactional
    public ResponseDto<?> logout(HttpServletRequest request) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        if (!tokenProvider.deleteRefreshToken(member)) {
            return ResponseDto.fail("존재하지 않는 Token 입니다.");
        }

        tokenProvider.saveBlacklistToken(request);

        return ResponseDto.success("로그아웃 성공");
    }

    // 이메일 중복 검사
    @Transactional(readOnly = true)
    public ResponseDto<String> checkEmail(DuplicationRequest request) {
        String regExp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
        if (!Pattern.matches(regExp, request.getValue())) {
            return ResponseDto.fail("이메일 양식을 지켜주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByEmail(request.getValue());
        if (optionalMember.isPresent()) {
            return ResponseDto.fail("사용중인 이메일 입니다.");
        }

        return ResponseDto.success("사용 가능한 이메일 입니다.");
    }

    // 닉네임 중복 검사
    @Transactional(readOnly = true)
    public ResponseDto<String> checkNickname(DuplicationRequest request) {
        String regExp = "^[가-힣a-zA-Z0-9]{2,10}$";
        if (!Pattern.matches(regExp, request.getValue())) {
            return ResponseDto.fail("2~10자리 한글,대소문자,숫자만 입력해주세요.");
        }

        Optional<Member> optionalMember = memberRepository.findByNickname(request.getValue());
        if (optionalMember.isPresent()) {
            return ResponseDto.fail("중복된 닉네임 입니다.");
        }

        return ResponseDto.success("사용 가능한 닉네임 입니다.");
    }

    // AccessToken, RefreshToken 재발급
    // RefreshToken이 유효하고 AccessToken이 만료되었을 때 새로 발급된다
    @Transactional
    public ResponseDto<String> reissue(HttpServletRequest request, HttpServletResponse response) throws ParseException {
        if (tokenProvider.getMemberIdByToken(request.getHeader("Authorization")) != null) {
            return ResponseDto.fail("아직 유효한 access token 입니다.");
        }
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return ResponseDto.fail("유효하지 않은 refresh token입니다.");
        }

        String memberId = tokenProvider.getMemberFromExpiredAccessToken(request);
        if (null == memberId) {
            return ResponseDto.fail("access token의 값이 유효하지 않습니다.");
        }

        Member member = memberRepository.findById(Long.parseLong(memberId)).orElse(null);
        if (null == member) {
            return ResponseDto.fail("memberId에 해당하는 멤버가 없습니다.");
        }

        RefreshToken refreshToken = tokenProvider.isPresentRefreshToken(member);

        if (!refreshToken.getKeyValue().equals(request.getHeader("RefreshToken"))) {
            log.info("refreshToken : " + refreshToken.getKeyValue());
            log.info("header rft : " + request.getHeader("RefreshToken"));
            return ResponseDto.fail("토큰이 일치하지 않습니다.");
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        refreshToken.updateValue(tokenDto.getRefreshToken());
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success("재발급 완료");
    }

    @Transactional
    public ResponseDto<?> withdrawal(HttpServletRequest request) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        if (member.getDeletedAt() != null) {
            return ResponseDto.fail("이미 탈퇴한 사용자입니다.");
        }

        if (!tokenProvider.deleteRefreshToken(member)) {
            return ResponseDto.fail("존재하지 않는 Token 입니다.");
        }

        memberRepository.delete(member);

        log.debug("Withdraw member -> memberId: {}", member.getId());
        return ResponseDto.success("Delete Success");
    }

}