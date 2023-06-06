package com.zerototen.savegame.member.domain.service;

import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.member.domain.controller.dto.SignUpForm;
import com.zerototen.savegame.member.domain.model.Member;
import com.zerototen.savegame.member.domain.repository.MemberRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpService {
  private final MemberRepository memberRepository;

  public boolean isEmailExist(String email){
    return memberRepository.findByEmail(email.toLowerCase(Locale.ROOT))
        .isPresent();
  }

  public boolean isNickNameExist(String nickName){
    return memberRepository.findByNickName(nickName.toLowerCase(Locale.ROOT))
        .isPresent();
  }

  public Member signUp(SignUpForm form){
    if(isNickNameExist(form.getNickName())){
      throw new CustomException(ErrorCode.ALREADY_REGISTER_NICKNAME);
    }
    if(isEmailExist(form.getEmail())){
      throw new CustomException(ErrorCode.ALREADY_REGISTER_EMAIL);
    }
    return memberRepository.save(Member.from(form));
  }

}
