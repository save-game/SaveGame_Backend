package com.zerototen.savegame.domain.dto.request;

import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.validation.Enum;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChallengeRequest {

    private String title;
    private String content;

    @Pattern(regexp = "\\d{1,2}/\\d{1,2}/\\d{4}", message = "유효하지 않은 날짜 형식입니다")
    private String startDate;

    @Pattern(regexp = "\\d{1,2}/\\d{1,2}/\\d{4}", message = "유효하지 않은 날짜 형식입니다")
    private String endDate;

    @Min(0)
    @Max(10000000)
    private int goalAmount;

    @Enum(enumClass = Category.class, ignoreCase = true, allowAll = true)
    private String category;

    @Min(1)
    @Max(10)
    private int maxPeople;

}