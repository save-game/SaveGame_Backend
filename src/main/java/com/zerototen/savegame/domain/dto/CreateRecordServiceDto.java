package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.dto.request.CreateRecordRequest;
import com.zerototen.savegame.domain.entity.Record;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import com.zerototen.savegame.util.ConvertUtil;
import java.time.LocalDate;
import java.util.Locale;
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

    public static CreateRecordServiceDto from(CreateRecordRequest request) {
        return CreateRecordServiceDto.builder()
            .amount(request.getAmount())
            .category(Category.valueOf(request.getCategory().toUpperCase(Locale.ROOT)))
            .paidFor(request.getPaidFor())
            .useDate(ConvertUtil.stringToLocalDate(request.getUseDate()))
            .payType(PayType.valueOf(request.getPayType().toUpperCase(Locale.ROOT)))
            .memo(request.getMemo())
            .build();
    }

}