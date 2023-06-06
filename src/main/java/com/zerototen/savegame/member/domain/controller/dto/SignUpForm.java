package com.zerototen.savegame.member.domain.controller.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpForm {
  private String email;
  private String password;
  private String nickName;

  public SignUpForm toServiceDto(){
    return new SignUpForm(email, password, nickName);
  }
}
