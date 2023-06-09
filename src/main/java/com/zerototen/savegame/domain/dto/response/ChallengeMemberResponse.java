package com.zerototen.savegame.domain.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeMemberResponse {

    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private int status;
    private long totalAmount;
    private List<ChallengeRecordResponse> recordList;

}