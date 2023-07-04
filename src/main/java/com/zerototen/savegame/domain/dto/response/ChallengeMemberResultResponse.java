package com.zerototen.savegame.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeMemberResultResponse {

    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private int status;
    private long totalAmount;

}