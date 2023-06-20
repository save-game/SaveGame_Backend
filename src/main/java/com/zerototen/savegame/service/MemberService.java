package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
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

    // 회원정보 조회
    public ResponseDto<?> getDetail(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_MEMBER.getDetail());
        }

        return ResponseDto.success(MemberResponse.from(member));
    }

    // 비밀번호 수정
    @Transactional
    public ResponseDto<?> updatePassword(Long memberId, UpdatePasswordRequest request) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_MEMBER.getDetail());
        }

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
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_MEMBER.getDetail());
        }

        if (memberRepository.findByNickname(request.getNickname()).isPresent()) {
            return ResponseDto.fail("중복된 닉네임 입니다.");
        }

        member.updateNickname(request);

        log.debug("Update nickname -> memberId: {}", memberId);
        return ResponseDto.success("Update Nickname Success");
    }

    // 회원 이미지 수정
    @Transactional
    public ResponseDto<?> updateProfileImageUrl(Long memberId, UpdateProfileImageUrlRequest request) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_MEMBER.getDetail());
        }

        member.updateProfileImageUrl(request);

        log.debug("Update profile image url -> memberId: {}", memberId);
        return ResponseDto.success("Update Profile Image Success");
    }

}