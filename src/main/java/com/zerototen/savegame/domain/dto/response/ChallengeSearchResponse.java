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
public class ChallengeSearchResponse {

    private Long challengeId;
    private String title;
    private String challengeContent;
    private int goalAmount;
    private LocalDate startDate;
    private int maxPeople;
    private int cnt;

}