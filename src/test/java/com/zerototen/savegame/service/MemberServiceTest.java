package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerototen.savegame.domain.dto.request.DuplicationRequest;
import com.zerototen.savegame.domain.dto.request.SignupRequest;
import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.util.PasswordUtil;
import java.time.LocalDateTime;
import java.util.Optional;
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

    @Nested
    @DisplayName("마이페이지")
    class testMypage {

        @Nested
        @DisplayName("조회")
        class GetDetail {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                Member member = getMember();

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(member));

                //when
                ResponseDto<?> responseDto = memberService.getDetail(1L);
                MemberResponse response = (MemberResponse) responseDto.getData();

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("*".repeat(8), response.getPassword());
                assertEquals(member.getEmail(), response.getEmail());
                assertEquals(member.getNickname(), response.getNickname());
                assertEquals(member.getProfileImageUrl(), response.getProfileImageUrl());
            }

            @Test
            @DisplayName("실패 - 존재하지 않는 사용자")
            void fail_NotFoundMember() {
                //given
                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                CustomException exception = assertThrows(CustomException.class, () -> memberService.getDetail(1L));

                //then
                assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
            }

        }

        @Nested
        @DisplayName("비밀번호 수정")
        class UpdatePassword {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                Member member = getMember();
                UpdatePasswordRequest request = getUpdatePasswordRequest("password11!!", "password11@@",
                    "password11@@");

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(member));

                //when
                ResponseDto<?> responseDto = memberService.updatePassword(1L, request);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Password Success", responseDto.getData());
                assertTrue(PasswordUtil.checkPassword(request.getNewPassword(), member.getPassword()));
            }

            @Nested
            @DisplayName("실패")
            class Fail {

                @Test
                @DisplayName("존재하지 않는 사용자")
                void notFoundMember() {
                    //given
                    UpdatePasswordRequest request = getUpdatePasswordRequest("password11!!", "password11@@",
                        "password11@@");

                    given(memberRepository.findById(anyLong()))
                        .willReturn(Optional.empty());

                    //when
                    CustomException exception = assertThrows(CustomException.class,
                        () -> memberService.updatePassword(1L, request));

                    //then
                    assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
                }

                @Test
                @DisplayName("비밀번호 확인 불일치")
                void notMatchNewPasswordCheck() {
                    //given
                    Member member = getMember();
                    UpdatePasswordRequest request = getUpdatePasswordRequest("password11!!", "password11@@",
                        "password11##");

                    given(memberRepository.findById(anyLong()))
                        .willReturn(Optional.of(member));

                    //when
                    ResponseDto<?> responseDto = memberService.updatePassword(1L, request);

                    //then
                    assertFalse(responseDto.isSuccess());
                    assertEquals("비밀번호가 일치하지 않습니다.", responseDto.getData());
                }

                @Test
                @DisplayName("이전 비밀번호 불일치")
                void notMatchOldPassword() {
                    //given
                    Member member = getMember();
                    UpdatePasswordRequest request = getUpdatePasswordRequest("password22!!", "password11@@",
                        "password11@@");

                    given(memberRepository.findById(anyLong()))
                        .willReturn(Optional.of(member));

                    //when
                    ResponseDto<?> responseDto = memberService.updatePassword(1L, request);

                    //then
                    assertFalse(responseDto.isSuccess());
                    assertEquals("이전 비밀번호가 일치하지 않습니다.", responseDto.getData());
                }
            }
        }

        @Nested
        @DisplayName("닉네임 수정")
        class UpdateNickname {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                Member member = getMember();
                UpdateNicknameRequest request = getUpdateNicknameRequest("newNickname");

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(member));

                //when
                ResponseDto<?> responseDto = memberService.updateNickname(1L, request);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Nickname Success", responseDto.getData());
                assertEquals(request.getNickname(), member.getNickname());
            }

            @Test
            @DisplayName("실패 - 존재하지 않는 사용자")
            void fail_NotFoundMember() {
                //given
                UpdateNicknameRequest request = getUpdateNicknameRequest("newNickname");

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> memberService.updateNickname(1L, request));

                //then
                assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
            }
        }

        @Nested
        @DisplayName("프로필 이미지 Url 수정")
        class UpdateProfileImageUrl {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                Member member = getMember();
                UpdateProfileImageUrlRequest request = getUpdateProfileImageUrlRequest(
                    "http://image.com/profile/1.png");

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(member));

                //when
                ResponseDto<?> responseDto = memberService.updateProfileImageUrl(1L, request);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Profile Image Url Success", responseDto.getData());
                assertEquals(request.getProfileImageUrl(), member.getProfileImageUrl());
            }

            @Test
            @DisplayName("실패 - 존재하지 않는 사용자")
            void fail_NotFoundMember() {
                //given
                UpdateProfileImageUrlRequest request = getUpdateProfileImageUrlRequest(
                    "http://image.com/profile/1.png");

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> memberService.updateProfileImageUrl(1L, request));

                //then
                assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
            }
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    public class testWithdrawal {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            Member member = getMember();

            given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));

            //when
            ResponseDto<?> responseDto = memberService.withdrawal(1L);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Delete Success", responseDto.getData());
        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("존재하지 않는 사용자")
            void notFoundMember() {
                //given
                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> memberService.withdrawal(1L));

                //then
                assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
            }

            @Test
            @DisplayName("이미 탈퇴한 사용자")
            void alreadyWithdrawnMember() {
                //given
                Member member = getMember();
                member.setDeletedAt(LocalDateTime.now());

                given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(member));

                //when
                ResponseDto<?> responseDto = memberService.withdrawal(1L);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals("이미 탈퇴한 사용자입니다.", responseDto.getData());
            }
        }
    }

    private static UpdatePasswordRequest getUpdatePasswordRequest(String oldPw, String newPw, String newPwChk) {
        return UpdatePasswordRequest.builder()
            .oldPassword(oldPw)
            .newPassword(newPw)
            .newPasswordCheck(newPwChk)
            .build();
    }

    private static UpdateNicknameRequest getUpdateNicknameRequest(String nickname) {
        return UpdateNicknameRequest.builder()
            .nickname(nickname)
            .build();
    }

    private static UpdateProfileImageUrlRequest getUpdateProfileImageUrlRequest(String url) {
        return UpdateProfileImageUrlRequest.builder()
            .profileImageUrl(url)
            .build();
    }

}