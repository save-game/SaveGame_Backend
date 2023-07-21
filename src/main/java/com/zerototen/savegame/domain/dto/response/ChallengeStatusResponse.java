package com.zerototen.savegame.domain.dto.response;

import com.zerototen.savegame.domain.type.Category;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeStatusResponse {

    private String title;
    private String content;
    private int challengeStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private int goalAmount;
    private Category category;
    private int maxPeople;
    private List<?> challengeMemberList;

}