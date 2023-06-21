package com.zerototen.savegame.domain.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNicknameRequest {

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;

}