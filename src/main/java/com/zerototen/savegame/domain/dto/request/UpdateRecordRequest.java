package com.zerototen.savegame.domain.dto.request;

import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import com.zerototen.savegame.util.ConvertUtil;
import com.zerototen.savegame.validation.Enum;
import java.util.Locale;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordRequest {

    @Min(1)
    private int amount;

    @Enum(enumClass = Category.class, ignoreCase = true)
    private String category;

    @NotBlank
    private String paidFor;

    private String memo;

    @Pattern(regexp = "\\d{1,2}/\\d{1,2}/\\d{4}", message = "유효하지 않은 날짜 형식입니다")
    private String useDate;

    @Enum(enumClass = PayType.class, ignoreCase = true)
    private String payType;

    public UpdateRecordServiceDto toServiceDto(Long id, Long memberId) {
        return UpdateRecordServiceDto.builder()
            .id(id)
            .memberId(memberId)
            .amount(this.getAmount())
            .category(Category.valueOf(this.getCategory().toUpperCase(Locale.ROOT)))
            .paidFor(this.getPaidFor())
            .useDate(ConvertUtil.stringToLocalDate(this.getUseDate()))
            .payType(PayType.valueOf(this.getPayType().toUpperCase(Locale.ROOT)))
            .memo(this.getMemo())
            .build();
    }

}