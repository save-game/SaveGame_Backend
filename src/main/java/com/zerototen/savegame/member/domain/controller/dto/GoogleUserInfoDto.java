package com.zerototen.savegame.member.domain.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GoogleUserInfoDto {
  private String sub;
  private String name;
  private String given_name;
  private String family_name;
  private String picture;
  private String email;
  private Boolean email_verified;
  private String locale;

}
