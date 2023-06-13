package com.zerototen.savegame.dto;

import com.zerototen.savegame.entity.Record;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordResponse {

    private Long recordId;
    private int amount;
    private String category;
    private String paidFor;
    private String memo;
    private LocalDate useDate;
    private String payType;

    public static RecordResponse from(Record record) {
        return RecordResponse.builder()
            .recordId(record.getId())
            .amount(record.getAmount())
            .category(record.getCategory().getName())
            .paidFor(record.getPaidFor())
            .memo(record.getMemo())
            .useDate(record.getUseDate())
            .payType(record.getPayType().getName())
            .build();
    }

}