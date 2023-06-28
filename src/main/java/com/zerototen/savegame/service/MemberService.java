package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.MemberChallengeResponse;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final TokenProvider tokenProvider;

    // 회원정보 조회
    public ResponseDto<?> getDetail(HttpServletRequest request) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        return ResponseDto.success(MemberResponse.from(member));
    }

    // 비밀번호 수정
    @Transactional
    public ResponseDto<?> updatePassword(HttpServletRequest request,
        UpdatePasswordRequest passwordRequest) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        if (!(new BCryptPasswordEncoder().matches(passwordRequest.getOldPassword(),
            member.getPassword()))) {
            return ResponseDto.fail("이전 비밀번호가 일치하지 않습니다.");
        }

        member.updatePassword(passwordRequest);
        memberRepository.save(member);
        log.debug("Update password -> memberId: {}", member.getId());
        return ResponseDto.success("Update Password Success");
    }

    // 닉네임 수정
    @Transactional
    public ResponseDto<?> updateNickname(HttpServletRequest request,
        UpdateNicknameRequest nicknameRequest) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        if (memberRepository.findByNickname(nicknameRequest.getNickname()).isPresent()) {
            return ResponseDto.fail("중복된 닉네임 입니다.");
        }

        member.updateNickname(nicknameRequest);
        memberRepository.save(member);
        log.debug("Update nickname -> memberId: {}", member.getId());
        return ResponseDto.success("Update Nickname Success");
    }

    // 회원 이미지 수정
    @Transactional
    public ResponseDto<?> updateProfileImageUrl(HttpServletRequest request,
        UpdateProfileImageUrlRequest imageUrlRequest) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        member.updateProfileImageUrl(imageUrlRequest);
        memberRepository.save(member);
        log.debug("Update profile image url -> memberId: {}", member.getId());
        return ResponseDto.success("Update Profile Image Success");
    }

    // 멤버 챌린지 조회
    public ResponseDto<?> getMemberChallengeList(HttpServletRequest request) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        return ResponseDto.success(
            challengeMemberRepository.findChallengeListByMemberOrderByEndDate(member).stream()
                .map(MemberChallengeResponse::from).collect(
                    Collectors.toList()));
    }

}