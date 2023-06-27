package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    AuthService authService;
    @Mock
    MemberRepository memberRepository;

    @Mock
    TokenProvider tokenProvider;

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
        authService.signup(request);

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
        String originPW = "password11!!";
        SignupRequest request = getSignupRequest(originPW);

        Member member = getMember();
        member.setEmail(request.getEmail());

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.of(member));

        //when
        ResponseDto<?> responseDto = authService.signup(request);

        //then
        assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL.getDetail(), responseDto.getData());
    }

    @Test
    @DisplayName("회원가입_실패_중복된 닉네임")
    void signUp_failure_ALREADY_EXIST_NICKNAME() {
        //given
        String originPW = "password11!!";
        SignupRequest request = getSignupRequest(originPW);

        Member member = getMember();
        member.setNickname(request.getNickname());

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        given(memberRepository.findByNickname(anyString()))
            .willReturn(Optional.of(member));

        //when
        ResponseDto<?> responseDto = authService.signup(request);

        //then
        assertEquals("중복된 닉네임 입니다.", responseDto.getData());
    }

    private static Member getMember() {
        return Member.builder()
            .id(1L)
            .email("test@naver.com")
            .password(new BCryptPasswordEncoder().encode("password11!!"))
            .nickname("testname")
            .profileImageUrl("/default.png")
            .userRole(Authority.ROLE_MEMBER)
            .build();
    }

    @Test
    @DisplayName("이메일 중복 검사_실패_이미 등록된 이메일")
    void checkEmail_fail_alreadyRegistered() {
        //given
        String value = "testnaver.com";
        Member member = getMember();

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.of(member));

        //when
        ResponseDto<?> responseDto = authService.checkEmail(value);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("이메일 중복 검사_실패_양식 미준수")
    void checkEmail_fail_formError() {
        //given
        String value = "testnaver.com";

        //when
        ResponseDto<?> responseDto = authService.checkEmail(value);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("이메일 중복 검사_성공")
    void checkEmail_success() {
        //given
        String value = "testnaver.com";

        given(memberRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        //when
        ResponseDto<?> responseDto = authService.checkEmail(value);

        //then
        assertTrue(responseDto.isSuccess());
    }

    @Test
    @DisplayName("닉네임 중복 검사_실패_양식 미준수")
    void checkNickname_fail_formError() {
        //given
        String value = "nick";

        //when
        ResponseDto<?> responseDto = authService.checkNickname(value);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("닉네임 중복 검사_실패_이미 등록된 닉네임")
    void checkNickname_fail_alreadyRegistered() {
        //given
        String value = "nick";
        Member member = getMember();

        given(memberRepository.findByNickname(anyString()))
            .willReturn(Optional.of(member));

        //when
        ResponseDto<?> responseDto = authService.checkNickname(value);

        //then
        assertFalse(responseDto.isSuccess());
    }

    @Test
    @DisplayName("닉네임 중복 검사_성공")
    void checkNickname_success() {
        //given
        String value = "nick";

        given(memberRepository.findByNickname(anyString()))
            .willReturn(Optional.empty());

        //when
        ResponseDto<?> responseDto = authService.checkNickname(value);

        //then
        assertTrue(responseDto.isSuccess());
    }

    @Nested
    @DisplayName("회원 탈퇴")
    public class testWithdrawal {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(tokenProvider.deleteRefreshToken(any(Member.class)))
                .willReturn(Boolean.TRUE);

            //when
            ResponseDto<?> responseDto = authService.withdrawal(request);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Delete Success", responseDto.getData());
        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("이미 탈퇴한 사용자")
            void alreadyWithdrawnMember() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                member.setDeletedAt(LocalDateTime.now());
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                //when
                ResponseDto<?> responseDto = authService.withdrawal(request);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals("이미 탈퇴한 사용자입니다.", responseDto.getData());
            }

            @Test
            @DisplayName("존재하지 않는 Refresh 토큰")
            void notExistRefreshToken() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(tokenProvider.deleteRefreshToken(any(Member.class)))
                    .willReturn(Boolean.FALSE);

                //when
                ResponseDto<?> responseDto = authService.withdrawal(request);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals("존재하지 않는 Token 입니다.", responseDto.getData());
            }
        }
    }

}