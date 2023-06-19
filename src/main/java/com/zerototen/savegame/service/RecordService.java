package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.entity.Record;
import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.RecordAnalysisResponse;
import com.zerototen.savegame.domain.dto.response.RecordResponse;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
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

    private final MemberRepository memberRepository;
    private final RecordRepository recordRepository;

    @Transactional
    public void create(CreateRecordServiceDto serviceDto) {
        memberRepository.findById(serviceDto.getMemberId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        recordRepository.save(serviceDto.toEntity());
        log.debug("Create record -> memberId: {}", serviceDto.getMemberId());
    }

    public List<RecordResponse> getInfos(Long memberId, LocalDate startDate, LocalDate endDate,
        List<String> categories) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.STARTDATE_AFTER_ENDDATE);
        }
        List<Record> records = recordRepository.findByMemberIdAndUseDateDescWithOptional(
            memberId, startDate, endDate, categories);

        return records.stream().map(RecordResponse::from).collect(Collectors.toList());
    }

    public List<RecordAnalysisResponse> getAnalysisInfo(Long memberId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return recordRepository.findByMemberIdAndUseDateAndAmountSumDesc(memberId, startDate, endDate)
            .stream().map(i -> {
                if (i.getCategory() == null) {
                    throw new CustomException(ErrorCode.CATEGORY_IS_NULL);
                }
                if (i.getTotal() == null || i.getTotal() <= 0) {
                    throw new CustomException(ErrorCode.INVALID_TOTAL);
                }
                return i.toResponse();
            }).collect(Collectors.toList());
    }

    @Transactional
    public void update(UpdateRecordServiceDto serviceDto) {
        Record record = recordRepository.findById(serviceDto.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getMemberId().equals(serviceDto.getMemberId())) {
            throw new CustomException(ErrorCode.NOT_MATCH_MEMBER);
        }

        record.update(serviceDto);
        log.debug("Update record -> id: {}", serviceDto.getId());
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        Record record = recordRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MATCH_MEMBER);
        }

        recordRepository.delete(record);
        log.debug("Delete record -> id: {}", id);
    }

}