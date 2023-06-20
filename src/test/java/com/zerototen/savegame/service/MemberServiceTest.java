package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerototen.savegame.domain.dto.request.DuplicationRequest;
import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    MemberService memberService;
    @Mock
    MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입_성공")
    void signUp_success() {
        // given
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String originPW = "password11!!";
        SignupRequest request = getSignupRequest(originPW);

        Member member = Member.builder()
            .id(1L)
            .build();
        given(memberRepository.save(any()))
            .willReturn(member);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        // when
        memberService.signup(request);

        // then
        verify(memberRepository, times(1)).save(captor.capture());
        assertEquals(captor.getValue().getEmail(), request.getEmail());
        assertEquals(captor.getValue().getNickname(), request.getNickname());
        assertTrue(encoder.matches(originPW, captor.getValue().getPassword()));
    }

    private static SignupRequest getSignupRequest(String originPW) {
        return SignupRequest.builder()
            .email("test@gmail.com")
            .password(originPW)
            .nickname("test")
            .build();
    }

    @Test
    @DisplayName("회원가입_실패_이미 등록된 이메일")
    void signUp_failure_ALREADY_REGISTERED_MEMBER() {
        //given
        Member member = getMember();

        String originPW = "password11!!";
        SignupRequest request = getSignupRequest(originPW);

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.of(member));

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> memberService.signup(request));

        //then
        assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL, exception.getErrorCode());
    }

    private static Member getMember() {
        return Member.builder()
            .id(1L)
            .email("test@naver.com")
            .nickname("testname")
            .userRole(Authority.ROLE_MEMBER)
            .build();
    }

    @Test
    @DisplayName("이메일 중복 검사_실패_이미 등록된 이메일")
    void checkEmail_fail_alreadyRegistered() {
        //given
        DuplicationRequest request = new DuplicationRequest("test@naver.com");
        Member member = getMember();

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.of(member));

        //when
        ResponseDto<?> responseDto = memberService.checkEmail(request);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("이메일 중복 검사_실패_양식 미준수")
    void checkEmail_fail_formError() {
        //given
        DuplicationRequest request = new DuplicationRequest("testnaver.com");

        //when
        ResponseDto<?> responseDto = memberService.checkEmail(request);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("이메일 중복 검사_성공")
    void checkEmail_success() {
        //given
        DuplicationRequest request = new DuplicationRequest("test2@naver.com");

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        //when
        ResponseDto<?> responseDto = memberService.checkEmail(request);

        //then
        assertTrue(responseDto.isSuccess());
    }

    @Test
    @DisplayName("닉네임 중복 검사_실패_양식 미준수")
    void checkNickname_fail_formError() {
        //given
        DuplicationRequest request = new DuplicationRequest("t");

        //when
        ResponseDto<?> responseDto = memberService.checkNickname(request);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("닉네임 중복 검사_실패_이미 등록된 닉네임")
    void checkNickname_fail_alreadyRegistered() {
        //given
        DuplicationRequest request = new DuplicationRequest("test");
        Member member = getMember();

        given(memberRepository.findByNickname(anyString()))
            .willReturn(Optional.of(member));

        //when
        ResponseDto<?> responseDto = memberService.checkNickname(request);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("닉네임 중복 검사_성공")
    void checkNickname_success() {
        //given
        DuplicationRequest request = new DuplicationRequest("test2");

        given(memberRepository.findByNickname(anyString()))
            .willReturn(Optional.empty());

        //when
        ResponseDto<?> responseDto = memberService.checkNickname(request);

        //then
        assertTrue(responseDto.isSuccess());
    }

}