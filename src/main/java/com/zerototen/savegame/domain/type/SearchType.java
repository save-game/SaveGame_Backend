package com.zerototen.savegame.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchType {

    ALL("전체"),
    TITLE("제목"),
    CONTENT("부제목");

    private final String name;

}