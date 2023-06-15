package com.zerototen.savegame.repository;

import com.zerototen.savegame.entity.Record;
import java.time.LocalDate;
import java.util.List;

public interface RecordRepositoryCustom {

    List<Record> findByMemberIdAndUseDateDescWithOptional(
        Long memberId, LocalDate startDate, LocalDate endDate, List<String> categories);

}