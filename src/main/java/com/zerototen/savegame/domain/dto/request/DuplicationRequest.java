package com.zerototen.savegame.domain.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DuplicationRequest {

    @NotBlank
    private String value;

}