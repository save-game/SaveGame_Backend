package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.entity.Record;
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
public class CreateRecordServiceDto {

    private int amount;
    private Category category;
    private String paidFor;
    private String memo;
    private LocalDate useDate;
    private PayType payType;

    public Record toEntity(Long memberId) {
        return Record.builder()
            .memberId(memberId)
            .amount(this.getAmount())
            .category(this.getCategory())
            .paidFor(this.getPaidFor())
            .useDate(this.getUseDate())
            .payType(this.getPayType())
            .memo(this.getMemo())
            .build();
    }

}