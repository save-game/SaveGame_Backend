package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.TokenDto;
import com.zerototen.savegame.domain.dto.request.DuplicationRequest;
import com.zerototen.savegame.domain.dto.request.LoginRequest;
import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.RefreshToken;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.CustomException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @Value("${image.default.profile}")
    private String defaultImageUrl; // TODO: 기본 이미지 URL 경로 프론트에 물어보기

    // 회원가입
    @Transactional
    public ResponseDto<?> signup(SignupRequest request) {
        Member member = getMemberByEmail(request.getEmail());
        if (member != null) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        //TODO: 기본 프로필 이미지 등록
        member = Member.builder()
            .email(request.getEmail())
            .password(new BCryptPasswordEncoder().encode(request.getPassword()))
            .nickname(request.getNickname())
            .profileImageUrl(defaultImageUrl)
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
    public ResponseDto<?> login(LoginRequest request, HttpServletResponse response) {
        Member member = getMemberByEmail(request.getEmail());
        if (member == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
        }

        // 비밀번호 확인
        if (!PasswordUtil.checkPassword(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
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
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return ResponseDto.fail("토큰 값이 올바르지 않습니다.");
        }

        // 맴버객체 찾아오기
        Member member = tokenProvider.getMemberFromAuthentication();
        if (null == member) {
            return ResponseDto.fail("사용자를 찾을 수 없습니다.");
        }
        if (tokenProvider.deleteRefreshToken(member)) {
            return ResponseDto.fail("존재하지 않는 Token 입니다.");
        }

        tokenProvider.deleteRefreshToken(member);

        return ResponseDto.success("로그아웃 성공");
    }

    // 이메일 중복 검사
    @Transactional(readOnly = true)
    public ResponseDto<?> checkEmail(DuplicationRequest request) {
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
    public ResponseDto<?> checkNickname(DuplicationRequest request) {
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

    // refresh token 재발급
    @Transactional
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response) throws ParseException {
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

    // 회원정보 조회
    public ResponseDto<?> getDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        return ResponseDto.success(MemberResponse.from(member));
    }

    // 비밀번호 수정
    @Transactional
    public ResponseDto<?> updatePassword(Long memberId, UpdatePasswordRequest request) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (!request.getNewPassword().equals(request.getNewPasswordCheck())) {
            return ResponseDto.fail("비밀번호가 일치하지 않습니다.");
        }

        if (!(new BCryptPasswordEncoder().matches(request.getOldPassword(), member.getPassword()))) {
            return ResponseDto.fail("이전 비밀번호가 일치하지 않습니다.");
        }

        member.updatePassword(request);

        log.debug("Update password -> memberId: {}", memberId);
        return ResponseDto.success("Update Password Success");
    }

    // 닉네임 수정
    @Transactional
    public ResponseDto<?> updateNickname(Long memberId, UpdateNicknameRequest request) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        member.updateNickname(request);

        log.debug("Update nickname -> memberId: {}", memberId);
        return ResponseDto.success("Update Nickname Success");
    }

    // 회원 이미지 수정
    @Transactional
    public ResponseDto<?> updateProfileImageUrl(Long memberId, UpdateProfileImageUrlRequest request) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        member.updateProfileImageUrl(request);

        log.debug("Update profile image url -> memberId: {}", memberId);
        return ResponseDto.success("Update Profile Image Url Success");
    }

    // 회원 탈퇴
    @Transactional
    public ResponseDto<?> withdrawal(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (member.getDeletedAt() != null) {
            return ResponseDto.fail("이미 탈퇴한 사용자입니다.");
        }

        tokenProvider.deleteRefreshToken(member);
        memberRepository.delete(member);

        log.debug("Withdrawal member -> memberId: {}", memberId);
        return ResponseDto.success("Delete Success");
    }

}