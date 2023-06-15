package com.zerototen.savegame.domain;

import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String paidFor;

    private String memo;

    @Column(nullable = false)
    private LocalDate useDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayType payType;

    public void update(UpdateRecordServiceDto serviceDto) {
        this.amount = serviceDto.getAmount();
        this.category = serviceDto.getCategory();
        this.paidFor = serviceDto.getPaidFor();
        this.memo = serviceDto.getMemo();
        this.useDate = serviceDto.getUseDate();
        this.payType = serviceDto.getPayType();
    }

}