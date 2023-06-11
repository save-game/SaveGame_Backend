package com.zerototen.savegame.member.domain.service;

import com.zerototen.savegame.member.domain.model.Member;
import com.zerototen.savegame.member.domain.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
  private final MemberRepository memberRepository;


  public Optional<Member> findValidUser(String email, String password){
    return memberRepository.findByEmail(email).stream().filter(
            user -> user.getPassword().equals(password))
        .findFirst();
  }
}
