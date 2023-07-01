package com.zerototen.savegame.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    ALL("전체"),
    FOOD("식비"),
    TRANSPORTATION("교통"),
    LIVING("생활"),
    HOUSE("주거"),
    CULTURE("문화"),
    CLOTHES("의류"),
    BEAUTY("뷰티"),
    MEDICAL("의료"),
    TELECOM("통신"),
    FINANCE("금융"),
    EVENT("경조사"),
    EDUCATION("교육"),
    PET("반려동물");

    private final String name;

}