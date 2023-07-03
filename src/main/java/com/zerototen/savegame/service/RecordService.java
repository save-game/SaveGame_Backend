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
import com.zerototen.savegame.repository.RecordRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final RecordRepository recordRepository;
    private final TokenProvider tokenProvider;

    // 지출 내역 등록
    @Transactional
    public ResponseDto<?> create(HttpServletRequest request, CreateRecordServiceDto serviceDto) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        log.debug("Create record -> memberId: {}", member.getId());
        return ResponseDto.success(recordRepository.save(Record.of(member, serviceDto)));
    }

    // 지출 내역 조회 (가계부 메인)
    public ResponseDto<?> getRecordList(HttpServletRequest request, LocalDate startDate,
        LocalDate endDate, List<String> categories) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        if (startDate.isAfter(endDate)) {
            return ResponseDto.fail(ErrorCode.STARTDATE_AFTER_ENDDATE.getDetail());
        }
        List<Record> records = recordRepository.findByMemberAndUseDateDescWithOptional(
            member, startDate, endDate, categories);

        return ResponseDto.success(
            records.stream().map(RecordResponse::from).collect(Collectors.toList()));
    }

    // 지출 내역 분석 (가계부 분석)
    public ResponseDto<?> getRecordAnalysis(HttpServletRequest request, int year, int month) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<RecordAnalysisServiceDto> serviceDtos =
            recordRepository.findByMemberAndUseDateAndAmountSumDesc(member, startDate, endDate);

        List<RecordAnalysisResponse> responses = new ArrayList<>();

        for (RecordAnalysisServiceDto serviceDto : serviceDtos) {
            if (serviceDto.getCategory() == null) {
                return ResponseDto.fail(ErrorCode.CATEGORY_IS_NULL.getDetail());
            }
            if (serviceDto.getTotal() == null || serviceDto.getTotal() <= 0) {
                return ResponseDto.fail(ErrorCode.INVALID_TOTAL.getDetail());
            }
            responses.add(RecordAnalysisResponse.from(serviceDto));
        }

        return ResponseDto.success(responses);
    }

    // 지출 내역 수정
    @Transactional
    public ResponseDto<?> update(HttpServletRequest request, UpdateRecordServiceDto serviceDto) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        Record record = recordRepository.findById(serviceDto.getId()).orElse(null);
        if (record == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_RECORD.getDetail());
        }

        if (!validateAuthority(record, member)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        record.update(serviceDto);
        log.debug("Update record -> id: {}", serviceDto.getId());
        return ResponseDto.success("Update Success");
    }

    // 지출 내역 삭제
    @Transactional
    public ResponseDto<?> delete(HttpServletRequest request, Long recordId) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        Record record = recordRepository.findById(recordId).orElse(null);
        if (record == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_RECORD.getDetail());
        }

        if (!validateAuthority(record, member)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        recordRepository.delete(record);
        log.debug("Delete record -> id: {}", recordId);
        return ResponseDto.success("Delete Success");
    }

    private boolean validateAuthority(Record record, Member member) {
        return record.getMember().getId().equals(member.getId());
    }

}