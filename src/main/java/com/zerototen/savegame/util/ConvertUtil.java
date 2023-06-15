package com.zerototen.savegame.util;

import java.time.LocalDate;

public class ConvertUtil {

    private static final StringToLocalDateConverter converter = new StringToLocalDateConverter();

    public static LocalDate stringToLocalDate(String date) {
        return converter.convert(date);
    }

}
