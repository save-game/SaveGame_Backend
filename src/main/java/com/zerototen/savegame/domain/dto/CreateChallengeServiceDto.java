package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.dto.request.CreateChallengeRequest;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.util.ConvertUtil;
import java.time.LocalDate;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChallengeServiceDto {

    private String title;
    private String challengeContent;
    private LocalDate startDate;
    private LocalDate endDate;
    private int goalAmount;
    private Category category;
    private int maxPeople;

    public static CreateChallengeServiceDto from(CreateChallengeRequest request) {
        return CreateChallengeServiceDto.builder()
            .title(request.getTitle())
            .challengeContent(request.getChallengeContent())
            .startDate(ConvertUtil.stringToLocalDate(request.getStartDate()))
            .endDate(ConvertUtil.stringToLocalDate(request.getEndDate()))
            .goalAmount(request.getGoalAmount())
            .category(Category.valueOf(request.getCategory().toUpperCase(Locale.ROOT)))
            .maxPeople(request.getMaxPeople())
            .build();
    }

}