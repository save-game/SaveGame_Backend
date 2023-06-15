package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.MemberDto;
import com.zerototen.savegame.domain.dto.MemberDto.LoginDto;
import com.zerototen.savegame.domain.dto.MemberDto.SaveDto;
import com.zerototen.savegame.domain.Member;
import com.zerototen.savegame.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입")
    void register() {

        // given
      MemberDto.SaveDto member = SaveDto.builder()
          .nickname("challenger")
          .email("challengers@challengers.com")
          .password("password!")
          .build();

        // when
        memberService.register(member);

        //then
        Member findMember = memberRepository.findByNickname("challenger").get();
        Assertions.assertThat(member.getNickname()).isEqualTo(findMember.getNickname());

    }

    @Test
    @DisplayName("로그인")
    @Transactional
    void login() {
        // given
        MemberDto.SaveDto member = SaveDto.builder()
            .nickname("challenger1")
            .email("challengers1@challengers.com")
            .password("password!")
            .build();
        memberService.register(member);

        // when
        MemberDto.LoginDto loginMember = LoginDto.builder()
            .email("challengers1@challengers.com")
            .password("password!")
            .build();

        //then
        Assertions.assertThat(memberService.login(loginMember).getStatusCodeValue()).isEqualTo(200);
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
        memberRepository.save(member);

        // then
        Assertions.assertThat(memberService.isEmailExist(member.getEmail())).isTrue();

    }

    @Test
    @DisplayName("이메일 존재하지 않을 때 False 반환")
    @Transactional
    void isEmailNotExist() {

        // given
        Member member = Member.builder()
            .email("test@test.com")
            .build();

        // when
        memberRepository.save(member);

        // then
        Assertions.assertThat(memberService.isEmailExist("fake@fake.com")).isFalse();

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
        memberRepository.save(member);

        // then
        Assertions.assertThat(memberService.isNicknameExist(member.getNickname())).isTrue();
    }

    @Test
    @DisplayName("닉네임 존재하지 않을 때 False 반환")
    @Transactional
    void isNicknameNotExist() {

        // given
        Member member = Member.builder()
            .nickname("nick")
            .build();

        // when
        memberRepository.save(member);

        // then
        Assertions.assertThat(memberService.isNicknameExist("name")).isFalse();
    }

}