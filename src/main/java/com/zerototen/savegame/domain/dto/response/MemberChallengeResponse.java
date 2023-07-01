package com.zerototen.savegame.domain.dto.response;

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

}