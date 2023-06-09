package com.zerototen.savegame.dto;

import com.zerototen.savegame.type.Category;
import com.zerototen.savegame.type.PayType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordServiceDto {

    private Long id;

    private Long memberId;

    private int amount;

    private Category category;

    private String store;

    private LocalDate useDate;

    private PayType payType;

    private String memo;

}
