package com.zerototen.savegame.service;

import com.zerototen.savegame.dto.CreateRecordServiceDto;
import com.zerototen.savegame.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.entity.Record;
import com.zerototen.savegame.repository.RecordRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;

    @Transactional
    public void create(CreateRecordServiceDto serviceDto) {
        // id에 해당하는 사용자 존재 여부 확인
        recordRepository.save(serviceDto.toEntity());
    }

    @Transactional
    public void update(UpdateRecordServiceDto serviceDto) {
        Record record = recordRepository.findById(serviceDto.getId())
            .orElseThrow(() -> new RuntimeException()); // Login과 연동 시 CustomException으로 수정 예정

        if (!record.getMemberId().equals(serviceDto.getMemberId())) {
            throw new RuntimeException(); // Login과 연동 시 CustomException으로 수정 예정
        }

        record.setAmount(serviceDto.getAmount());
        record.setCategory(serviceDto.getCategory());
        record.setStore(serviceDto.getStore());
        record.setUseDate(serviceDto.getUseDate());
        record.setPayType(serviceDto.getPayType());
        record.setMemo(serviceDto.getMemo());
    }
}
