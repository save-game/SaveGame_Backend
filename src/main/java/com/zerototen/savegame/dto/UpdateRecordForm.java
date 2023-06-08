package com.zerototen.savegame.dto;

import com.zerototen.savegame.type.Category;
import com.zerototen.savegame.type.PayType;
import com.zerototen.savegame.validation.Enum;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordForm {

    @Min(1)
    private int amount;

    @Enum(enumClass = Category.class, ignoreCase = true)
    private String category;

    @NotBlank
    private String store;

    @NotNull
    private LocalDate useDate;

    @Enum(enumClass = PayType.class, ignoreCase = true)
    private String payType;

    private String memo;

    public UpdateRecordServiceDto toServiceDto(Long memberId) {
        return UpdateRecordServiceDto.builder()
            .memberId(memberId)
            .amount(this.getAmount())
            .category(Category.valueOf(this.getCategory()))
            .store(this.getStore())
            .useDate(this.getUseDate())
            .payType(PayType.valueOf(this.getPayType()))
            .memo(this.getMemo())
            .build();
    }
}
