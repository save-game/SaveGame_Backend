package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.util.PasswordUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
                ResponseDto<?> responseDto = memberService.getDetail(1L);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.NOT_FOUND_MEMBER.getDetail(), responseDto.getData());
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
                    ResponseDto<?> responseDto = memberService.updatePassword(1L, request);

                    //then
                    assertFalse(responseDto.isSuccess());
                    assertEquals(ErrorCode.NOT_FOUND_MEMBER.getDetail(), responseDto.getData());
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

                given(memberRepository.findByNickname(anyString()))
                    .willReturn(Optional.empty());

                //when
                ResponseDto<?> responseDto = memberService.updateNickname(1L, request);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Nickname Success", responseDto.getData());
                assertEquals(request.getNickname(), member.getNickname());
            }

            @Nested
            @DisplayName("실패")
            class Fail {

                @Test
                @DisplayName("실패 - 존재하지 않는 사용자")
                void notFoundMember() {
                    //given
                    UpdateNicknameRequest request = getUpdateNicknameRequest("newNickname");

                    given(memberRepository.findById(anyLong()))
                        .willReturn(Optional.empty());

                    //when
                    ResponseDto<?> responseDto = memberService.updateNickname(1L, request);

                    //then
                    assertFalse(responseDto.isSuccess());
                    assertEquals(ErrorCode.NOT_FOUND_MEMBER.getDetail(), responseDto.getData());
                }

                @Test
                @DisplayName("실패 - 중복된 닉네임")
                void alreadyExistNickname() {
                    //given
                    Member member = getMember();
                    UpdateNicknameRequest request = getUpdateNicknameRequest("newNickname");

                    Member sameNicknameMember = getMember();
                    sameNicknameMember.setId(2L);
                    sameNicknameMember.setEmail("same@gmail.com");
                    sameNicknameMember.setNickname(request.getNickname());

                    given(memberRepository.findById(anyLong()))
                        .willReturn(Optional.of(member));

                    given(memberRepository.findByNickname(anyString()))
                        .willReturn(Optional.of(sameNicknameMember));

                    //when
                    ResponseDto<?> responseDto = memberService.updateNickname(1L, request);

                    //then
                    assertFalse(responseDto.isSuccess());
                    assertEquals("중복된 닉네임 입니다.", responseDto.getData());
                }
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
                assertEquals("Update Profile Image Success", responseDto.getData());
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
                ResponseDto<?> responseDto = memberService.updateProfileImageUrl(1L, request);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.NOT_FOUND_MEMBER.getDetail(), responseDto.getData());
            }
        }
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