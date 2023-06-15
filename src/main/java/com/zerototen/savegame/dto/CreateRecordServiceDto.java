package com.zerototen.savegame.dto;

import com.zerototen.savegame.entity.Record;
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
public class CreateRecordServiceDto {

    private Long memberId;
    private int amount;
    private Category category;
    private String paidFor;
    private String memo;
    private LocalDate useDate;
    private PayType payType;

    public Record toEntity() {
        return Record.builder()
            .memberId(this.getMemberId())
            .amount(this.getAmount())
            .category(this.getCategory())
            .paidFor(this.getPaidFor())
            .useDate(this.getUseDate())
            .payType(this.getPayType())
            .memo(this.getMemo())
            .build();
    }

}