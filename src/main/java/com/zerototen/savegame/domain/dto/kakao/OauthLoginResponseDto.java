package com.zerototen.savegame.domain.dto.kakao;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OauthLoginResponseDto {

    private String email;
    private String nickname;
    private String profileImageUrl;

}