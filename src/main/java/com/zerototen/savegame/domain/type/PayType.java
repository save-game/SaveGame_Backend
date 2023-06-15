package com.zerototen.savegame.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayType {

    CASH("현금"),
    CARD("카드");

    private final String name;

}