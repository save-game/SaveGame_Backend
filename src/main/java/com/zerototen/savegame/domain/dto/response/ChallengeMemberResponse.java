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
    private int status;
    private List<ChallengeRecordResponse> recordList;

}