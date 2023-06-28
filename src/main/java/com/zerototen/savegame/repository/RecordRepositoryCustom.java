package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Record;

import java.time.LocalDate;
import java.util.List;

public interface RecordRepositoryCustom {

    List<Record> findByMemberAndUseDateDescWithOptional(
            Member member, LocalDate startDate, LocalDate endDate, List<String> categories);

    List<RecordAnalysisServiceDto> findByMemberAndUseDateAndAmountSumDesc(Member member, LocalDate startDate,
                                                                          LocalDate endDate);

}