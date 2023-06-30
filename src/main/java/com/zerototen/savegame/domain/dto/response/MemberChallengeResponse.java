package com.zerototen.savegame.domain.dto.response;

import com.zerototen.savegame.domain.entity.Challenge;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberChallengeResponse {

    private Long challengeId;

    private String title;

    private LocalDate endDate;

    public static MemberChallengeResponse from(Challenge challenge) {
        return MemberChallengeResponse.builder()
            .challengeId(challenge.getId())
            .title(challenge.getTitle())
            .endDate(challenge.getEndDate())
            .build();
    }

}