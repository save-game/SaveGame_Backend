package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.Record;
import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import java.time.LocalDate;
import java.util.List;

public interface RecordRepositoryCustom {

    List<Record> findByMemberIdAndUseDateDescWithOptional(
        Long memberId, LocalDate startDate, LocalDate endDate, List<String> categories);

    List<RecordAnalysisServiceDto> findByMemberIdAndUseDateAndAmountSumDesc(Long memberId, LocalDate startDate,
        LocalDate endDate);

}