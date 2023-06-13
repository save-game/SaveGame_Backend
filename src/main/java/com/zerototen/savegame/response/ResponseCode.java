package com.zerototen.savegame.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    CREATE_SUCCESS(HttpStatus.CREATED, "Create Success"),
    UPDATE_SUCCESS(HttpStatus.OK, "Update Success"),
    DELETE_SUCCESS(HttpStatus.OK, "Delete Success");

    private final HttpStatus status;
    private final String message;

}