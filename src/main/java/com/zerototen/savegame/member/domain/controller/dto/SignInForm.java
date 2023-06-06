package com.zerototen.savegame.member.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInForm {
  private String email;
  private String password;

  public SignInForm toServiceDto(){
    return new SignInForm(email, password);
  }
}
