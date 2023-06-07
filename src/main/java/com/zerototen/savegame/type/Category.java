package com.zerototen.savegame.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    FOOD("식비"),
    TRANSPORTATION("교통비"),
    CULTURE("문화생활"),
    NECESSITY("생필품"),
    CLOTHES("의류"),
    BEAUTY("미용"),
    HEALTH("의료/건강"),
    EDUCATION("교육"),
    COMMUNICATION("통신비"),
    MEMBERSHIP("회비"),
    CONDOLENCES("경조사"),
    SAVING("저축"),
    ELECTRONICS("가전"),
    UTILITIES("공과금"),
    CARD("카드대금");

    private final String name;
}
