package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
import com.zerototen.savegame.domain.dto.response.MemberChallengeResponse;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.domain.type.ChallengeStatus;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.security.TokenProvider;
import com.zerototen.savegame.util.PasswordUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    MemberService memberService;
    @Mock
    MemberRepository memberRepository;

    @Mock
    ChallengeMemberRepository challengeMemberRepository;

    @Mock
    TokenProvider tokenProvider;

    @Nested
    @DisplayName("마이페이지")
    class testMypage {

        @Test
        @DisplayName("조회 성공")
        void getDetailSuccess() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            //when
            ResponseDto<?> responseDto = memberService.getDetail(request);
            MemberResponse response = (MemberResponse) responseDto.getData();

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals(member.getEmail(), response.getEmail());
            assertEquals(member.getNickname(), response.getNickname());
            assertEquals(member.getProfileImageUrl(), response.getProfileImageUrl());
        }

        @Nested
        @DisplayName("비밀번호 수정")
        class UpdatePassword {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                UpdatePasswordRequest passwordRequest = getUpdatePasswordRequest("password11!!",
                    "password11@@");

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                //when
                ResponseDto<?> responseDto = memberService.updatePassword(request, passwordRequest);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Password Success", responseDto.getData());
                assertTrue(PasswordUtil.checkPassword(passwordRequest.getNewPassword(),
                    member.getPassword()));
            }

            @Test
            @DisplayName("이전 비밀번호 불일치")
            void fail_notMatchOldPassword() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                UpdatePasswordRequest passwordRequest = getUpdatePasswordRequest("password22!!",
                    "password11@@");

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                //when
                ResponseDto<?> responseDto = memberService.updatePassword(request, passwordRequest);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals("이전 비밀번호가 일치하지 않습니다.", responseDto.getData());
            }
        }

        @Nested
        @DisplayName("닉네임 수정")
        class UpdateNickname {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                UpdateNicknameRequest nicknameRequest = getUpdateNicknameRequest("newNickname");

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(memberRepository.findByNickname(anyString()))
                    .willReturn(Optional.empty());

                //when
                ResponseDto<?> responseDto = memberService.updateNickname(request, nicknameRequest);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Nickname Success", responseDto.getData());
                assertEquals(nicknameRequest.getNickname(), member.getNickname());
            }

            @Test
            @DisplayName("실패 - 중복된 닉네임")
            void fail_AlreadyExistNickname() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                UpdateNicknameRequest nicknameRequest = getUpdateNicknameRequest("newNickname");

                Member sameNicknameMember = getMember();
                sameNicknameMember.setId(2L);
                sameNicknameMember.setEmail("same@gmail.com");
                sameNicknameMember.setNickname(nicknameRequest.getNickname());

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(memberRepository.findByNickname(anyString()))
                    .willReturn(Optional.of(sameNicknameMember));

                //when
                ResponseDto<?> responseDto = memberService.updateNickname(request, nicknameRequest);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals("중복된 닉네임 입니다.", responseDto.getData());
            }
        }

        @Nested
        @DisplayName("프로필 이미지 Url 수정")
        class UpdateProfileImageUrl {

            @Test
            @DisplayName("성공")
            void success() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                UpdateProfileImageUrlRequest imageUrlRequest = getUpdateProfileImageUrlRequest(
                    "http://image.com/profile/1.png");

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                //when
                ResponseDto<?> responseDto = memberService.updateProfileImageUrl(request,
                    imageUrlRequest);

                //then
                assertTrue(responseDto.isSuccess());
                assertEquals("Update Profile Image Success", responseDto.getData());
                assertEquals(imageUrlRequest.getProfileImageUrl(), member.getProfileImageUrl());
            }
        }
    }

    @Test
    @DisplayName("사용자 챌린지 조회 - 성공")
    void testGetMemberChallengeList_Success() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
        Pageable pageable = PageRequest.of(0, 3);
        Page<MemberChallengeResponse> responsePage = getMemberChallengePage(member, pageable);

        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

        given(challengeMemberRepository.findChallengeListByMemberOrderByEndDate(any(Member.class),
            any(ChallengeStatus.class), any(Pageable.class)))
            .willReturn(responsePage);

        // when
        ResponseDto<?> responseDto = memberService.getMemberChallengeList(request, "ONGOING",
            pageable);
        Page<MemberChallengeResponse> response = (Page<MemberChallengeResponse>) responseDto.getData();

        // then
        assertTrue(responseDto.isSuccess());
        assertEquals(pageable.getPageNumber(), response.getPageable().getPageNumber());
        int size = response.getPageable().getPageSize();
        for (int i = 0; i < size; i++) {
            assertEquals(i + 1, response.getContent().get(i).getChallengeId());
            assertEquals("제목" + (i + 1), response.getContent().get(i).getTitle());
            assertEquals(LocalDate.now().plusDays(i + 1),
                response.getContent().get(i).getEndDate());
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

    private static UpdatePasswordRequest getUpdatePasswordRequest(String oldPw, String newPw) {
        return UpdatePasswordRequest.builder()
            .oldPassword(oldPw)
            .newPassword(newPw)
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

    private static Page<MemberChallengeResponse> getMemberChallengePage(Member member,
        Pageable pageable) {

        List<MemberChallengeResponse> responseList = new ArrayList<>();

        for (int i = 1; i <= pageable.getPageSize(); i++) {
            responseList.add(MemberChallengeResponse.builder()
                .challengeId((long) i)
                .title("제목" + i)
                .endDate(LocalDate.now().plusDays(i))
                .build());
        }

        return new PageImpl<>(responseList, pageable, responseList.size());
    }

}