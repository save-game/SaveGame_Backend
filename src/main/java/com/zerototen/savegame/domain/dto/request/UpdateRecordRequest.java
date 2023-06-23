package com.zerototen.savegame.domain.dto.request;

import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import com.zerototen.savegame.validation.Enum;
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

}