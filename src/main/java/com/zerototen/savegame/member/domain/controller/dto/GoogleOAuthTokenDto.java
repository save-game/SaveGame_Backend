package com.zerototen.savegame.member.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GoogleOAuthTokenDto {
  private String access_token;
  private Integer expires_in;
  private String scope;
  private String token_type;
  private String id_token;
}
