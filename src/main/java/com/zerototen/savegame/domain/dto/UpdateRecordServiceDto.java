package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
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
    private String paidFor;
    private String memo;
    private LocalDate useDate;
    private PayType payType;

}