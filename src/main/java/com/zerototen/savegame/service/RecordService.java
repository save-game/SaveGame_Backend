package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.dto.response.RecordAnalysisResponse;
import com.zerototen.savegame.domain.dto.response.RecordResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Record;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.repository.RecordRepository;
import java.time.LocalDate;
import java.util.ArrayList;
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

    // 지출 내역 등록
    @Transactional
    public ResponseDto<?> create(CreateRecordServiceDto serviceDto) {
        Member member = memberRepository.findById(serviceDto.getMemberId()).orElse(null);
        if (member == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_MEMBER.getDetail());
        }

        log.debug("Create record -> memberId: {}", serviceDto.getMemberId());
        return ResponseDto.success(recordRepository.save(serviceDto.toEntity()));
    }

    // 지출 내역 조회 (가계부 메인)
    public ResponseDto<?> getInfos(Long memberId, LocalDate startDate, LocalDate endDate,
        List<String> categories) {
        if (startDate.isAfter(endDate)) {
            return ResponseDto.fail(ErrorCode.STARTDATE_AFTER_ENDDATE.getDetail());
        }
        List<Record> records = recordRepository.findByMemberIdAndUseDateDescWithOptional(
            memberId, startDate, endDate, categories);

        return ResponseDto.success(records.stream().map(RecordResponse::from).collect(Collectors.toList()));
    }

    // 지출 내역 분석 (가계부 분석)
    public ResponseDto<?> getAnalysisInfo(Long memberId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<RecordAnalysisServiceDto> serviceDtos = recordRepository.findByMemberIdAndUseDateAndAmountSumDesc(memberId,
            startDate, endDate);

        List<RecordAnalysisResponse> responses = new ArrayList<>();

        for (RecordAnalysisServiceDto serviceDto : serviceDtos) {
            if (serviceDto.getCategory() == null) {
                return ResponseDto.fail(ErrorCode.CATEGORY_IS_NULL.getDetail());
            }
            if (serviceDto.getTotal() == null || serviceDto.getTotal() <= 0) {
                return ResponseDto.fail(ErrorCode.INVALID_TOTAL.getDetail());
            }
            responses.add(serviceDto.toResponse());
        }

        return ResponseDto.success(responses);
    }

    // 지출 내역 수정
    @Transactional
    public ResponseDto<?> update(UpdateRecordServiceDto serviceDto) {
        Record record = recordRepository.findById(serviceDto.getId()).orElse(null);
        if (record == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_RECORD.getDetail());
        }

        if (!record.getMemberId().equals(serviceDto.getMemberId())) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        record.update(serviceDto);
        log.debug("Update record -> id: {}", serviceDto.getId());
        return ResponseDto.success("Update Success");
    }

    // 지출 내역 삭제
    @Transactional
    public ResponseDto<?> delete(Long id, Long memberId) {
        Record record = recordRepository.findById(id).orElse(null);
        if (record == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_RECORD.getDetail());
        }

        if (!record.getMemberId().equals(memberId)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        recordRepository.delete(record);
        log.debug("Delete record -> id: {}", id);
        return ResponseDto.success("Delete Success");
    }

}