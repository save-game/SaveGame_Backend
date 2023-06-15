package com.zerototen.savegame.member.domain.controller.dto;

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
    private String nickname;

    public SignUpForm toServiceDto() {
        return new SignUpForm(email, password, nickname);
    }

}