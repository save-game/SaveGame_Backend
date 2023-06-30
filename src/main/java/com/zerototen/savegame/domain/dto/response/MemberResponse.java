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

    private Long memberId;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
            .memberId(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .profileImageUrl(member.getProfileImageUrl())
            .build();
    }

}