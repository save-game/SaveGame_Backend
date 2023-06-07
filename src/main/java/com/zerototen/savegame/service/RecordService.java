package com.zerototen.savegame.service;

import com.zerototen.savegame.dto.CreateRecordServiceDto;
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

}
