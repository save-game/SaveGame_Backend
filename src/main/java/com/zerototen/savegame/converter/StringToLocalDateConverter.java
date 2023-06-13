package com.zerototen.savegame.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private final DateTimeFormatter formatter;

    public StringToLocalDateConverter() {
        this.formatter = DateTimeFormatter.ofPattern("[uuuu.M.d][uuuu.M.dd][uuuu.MM.d][uuuu.MM.dd]")
            .withResolverStyle(ResolverStyle.STRICT);
    }

    @Override
    public LocalDate convert(String date) {
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", date);
            throw new IllegalArgumentException("유효하지 않은 날짜 형식입니다 : " + date);
        }
    }

}