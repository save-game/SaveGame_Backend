package com.zerototen.savegame.domain.dto.response;

import com.zerototen.savegame.domain.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private String email;
    private String nickname;
    private String password;
    private String profileImageUrl;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
            .email(member.getEmail())
            .nickname(member.getNickname())
            .password("*".repeat(8)) // TODO: 원래 비밀번호 길이를 구할수 없으므로 일단 *만 8자리 보냄, 다른방법 있는지 확인
            .profileImageUrl(member.getProfileImageUrl())
            .build();
    }

}
