package com.zerototen.savegame.domain.entity;

import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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

    public static Record of(Member member, CreateRecordServiceDto serviceDto) {
        return Record.builder()
                .member(member)
                .amount(serviceDto.getAmount())
                .category(serviceDto.getCategory())
                .paidFor(serviceDto.getPaidFor())
                .useDate(serviceDto.getUseDate())
                .payType(serviceDto.getPayType())
                .memo(serviceDto.getMemo())
                .build();
    }

}