package com.zerototen.savegame.service;

import static org.mockito.Mockito.when;
import com.zerototen.savegame.config.jwt.TokenProvider;
import com.zerototen.savegame.domain.Member;
import com.zerototen.savegame.domain.dto.MemberDto;
import com.zerototen.savegame.domain.dto.MemberDto.DeleteDto;
import com.zerototen.savegame.domain.dto.MemberDto.LoginDto;
import com.zerototen.savegame.domain.dto.MemberDto.SaveDto;
import com.zerototen.savegame.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;


@ExtendWith(MockitoExtension.class)
class MemberServiceTest {


    static MemberRepository memberRepository;
    static MemberService memberService;
    static Member member;
    static TokenProvider tokenProvider;
    String accessToken = "dummy accessToken";
    String refreshToken = "dummy refreshToken";


    @BeforeAll
    static void beforeAll(){


        memberService = Mockito.mock(MemberService.class);
        memberRepository = Mockito.mock(MemberRepository.class);
        tokenProvider = Mockito.mock(TokenProvider.class);

    }


    @Test
    @DisplayName("회원가입")
    @Transactional
    void register() {

        // given

        SaveDto newMember = SaveDto.builder()
            .nickname("challenger")
            .password("Password!")
            .email("Challenger@challenge.com")
            .build();

             member = new Member();
             member.setId(1L);
             member.setEmail(newMember.getEmail());
             member.setNickname(newMember.getNickname());
             member.setPassword(newMember.getPassword());


             when(memberService.register(newMember)).thenReturn(new
                 ResponseEntity<>(MemberDto.SaveDto.response(member, accessToken, refreshToken),
                 HttpStatus.OK));

             when(memberRepository.findByNickname(member.getNickname())).thenReturn(Optional.ofNullable(member));
             when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.ofNullable(member));

             // then
            Assertions.assertThat(member.getNickname()).isEqualTo(newMember.getNickname());
            Assertions.assertThat(member.getEmail()).isEqualTo(newMember.getEmail());

    }

    @Test
    @DisplayName("로그인")
    @Transactional
    void login() {

        // given
        SaveDto newMember = SaveDto.builder()
            .nickname("challenger")
            .password("Password!")
            .email("Challenger@challenge.com")
            .build();

        member = new Member();
        member.setId(1L);
        member.setEmail(newMember.getEmail());
        member.setNickname(newMember.getNickname());
        member.setPassword(newMember.getPassword());


        when(memberService.register(newMember)).thenReturn(new
            ResponseEntity<>(MemberDto.SaveDto.response(member, accessToken, refreshToken),
            HttpStatus.OK));

        LoginDto existMember = LoginDto.builder()
            .email(member.getEmail())
            .password(member.getPassword())
            .build();

        when(memberService.login(existMember)).thenReturn(new
            ResponseEntity<>(MemberDto.LoginDto.response(accessToken, refreshToken),
            HttpStatus.OK));


        Assertions.assertThat(MemberDto.LoginDto.response(accessToken, refreshToken).getAccessToken())
            .isEqualTo("dummy accessToken");
        Assertions.assertThat(MemberDto.LoginDto.response(accessToken, refreshToken).getRefreshToken())
            .isEqualTo("dummy refreshToken");

    }

    @Test
    @DisplayName("회원삭제")
    @Transactional
    void delete() {

        // given
        SaveDto newMember = SaveDto.builder()
            .password("deleteEmail!")
            .email("delete@email.com")
            .build();

        member = new Member();
        member.setId(1L);
        member.setEmail(newMember.getEmail());
        member.setPassword(newMember.getPassword());

        when(memberService.register(newMember)).thenReturn(new
            ResponseEntity<>(MemberDto.SaveDto.response(member, accessToken, refreshToken),
            HttpStatus.OK));

        DeleteDto deleteMember = DeleteDto.builder()
            .email(newMember.getEmail())
            .password(newMember.getPassword())
            .build();

        when(memberService.delete(deleteMember)).thenReturn(new
            ResponseEntity<>("삭제가 완료되었습니다.", HttpStatus.OK));

        when(memberRepository.findByEmail(deleteMember.getEmail())).thenReturn(
            Optional.ofNullable(member));

        member.setDeletedAt(LocalDateTime.now());

        Assertions.assertThat(memberRepository.findByEmail(deleteMember.getEmail()).get().getDeletedAt()).isNotNull();

    }

    @Test
    @DisplayName("이메일 존재 할 때 True 반환")
    @Transactional
    void isEmailExist() {

        // given
        Member member = Member.builder()
            .email("test@test.com")
            .build();

        // when
        when(memberRepository.save(member)).thenReturn(member);
        when(memberService.isEmailExist(member.getEmail())).thenReturn(true);

        // then
        Assertions.assertThat(memberService.isEmailExist(member.getEmail())).isTrue();

    }

    @Test
    @DisplayName("이메일 존재하지 않을 때 False 반환")
    @Transactional
    void isEmailNotExist() {

        // given
        String fakeEmail = "dslfkjawdslfjk@sdlfkjasl.com";

        // when
        when(memberService.isEmailExist(fakeEmail)).thenReturn(false);

        // then
        Assertions.assertThat(memberService.isEmailExist(fakeEmail)).isFalse();

    }

    @Test
    @DisplayName("닉네임 존재할 때 True 반환")
    @Transactional
    void isNicknameExist() {

        // given
        Member member = Member.builder()
            .nickname("nick")
            .build();

        // when
        when(memberRepository.save(member)).thenReturn(member);
        when(memberService.isNicknameExist(member.getNickname())).thenReturn(true);

        // then
        Assertions.assertThat(memberService.isNicknameExist(member.getNickname())).isTrue();

    }

    @Test
    @DisplayName("닉네임 존재하지 않을 때 False 반환")
    @Transactional
    void isNicknameNotExist() {

        // given
        String fakeNickname = "sdlfkjaesltfjksa";

        // when
        when(memberService.isNicknameExist(fakeNickname)).thenReturn(false);

        // then
        Assertions.assertThat(memberService.isNicknameExist(fakeNickname)).isFalse();
    }

}