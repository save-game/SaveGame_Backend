package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.RecordResponse;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.Record;
import com.zerototen.savegame.repository.RecordRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final RecordRepository recordRepository;

    @Transactional
    public void create(CreateRecordServiceDto serviceDto) {
        // Login과 연동 시 memberId 검증 부분 추가
        recordRepository.save(serviceDto.toEntity());
        log.debug("Create record -> memberId: {}", serviceDto.getMemberId());
    }

    public List<RecordResponse> getInfos(Long memberId, LocalDate startDate, LocalDate endDate,
        List<String> categories) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("조회시작일이 조회종료일 이후입니다"); // Login과 연동 시 CustomException으로 수정 예정
        }
        List<Record> records = recordRepository.findByMemberIdAndUseDateDescWithOptional(
            memberId, startDate, endDate, categories);

        return records.stream().map(RecordResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void update(UpdateRecordServiceDto serviceDto) {
        Record record = recordRepository.findById(serviceDto.getId())
            .orElseThrow(() -> new RuntimeException("Not found record")); // Login과 연동 시 CustomException으로 수정 예정

        if (!record.getMemberId().equals(serviceDto.getMemberId())) {
            throw new RuntimeException("Not match member"); // Login과 연동 시 CustomException으로 수정 예정
        }

        record.update(serviceDto);
        log.debug("Update record -> id: {}", serviceDto.getId());
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        Record record = recordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Not found record")); // Login과 연동 시 CustomException으로 수정 예정

        if (!record.getMemberId().equals(memberId)) {
            throw new RuntimeException("Not match member"); // Login과 연동 시 CustomException으로 수정 예정
        }

        recordRepository.delete(record);
        log.debug("Delete record -> id: {}", id);
    }

}