package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.type.Authority;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupInfoDto {

    String email;
    String nickname;
    String imgUrl;
    Authority role;

}