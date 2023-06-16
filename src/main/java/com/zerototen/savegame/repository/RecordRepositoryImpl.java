package com.zerototen.savegame.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerototen.savegame.domain.QRecord;
import com.zerototen.savegame.domain.Record;
import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import com.zerototen.savegame.domain.type.Category;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordRepositoryImpl implements RecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Record> findByMemberIdAndUseDateDescWithOptional(
        Long memberId, LocalDate startDate, LocalDate endDate, List<String> categories) {

        QRecord record = QRecord.record;

        BooleanExpression condition = record.memberId.eq(memberId)
            .and(record.useDate.between(startDate, endDate));

        if (categories != null && !categories.isEmpty()) {
            condition = condition.and(record.category.in(
                categories.stream().map(Category::valueOf).collect(Collectors.toList())));
        }

        return queryFactory.selectFrom(record).where(condition).orderBy(record.useDate.desc())
            .fetch();
    }

    @Override
    public List<RecordAnalysisServiceDto> findByMemberIdAndUseDateAndAmountSumDesc(Long memberId, LocalDate startDate,
        LocalDate endDate) {
        QRecord record = QRecord.record;

        List<Tuple> result = queryFactory.select(record.category, record.amount.longValue().sum()).from(record)
            .where(record.memberId.eq(memberId).and(record.useDate.between(startDate, endDate)))
            .groupBy(record.category).orderBy(record.amount.longValue().sum().desc()).fetch();

        return result.stream().map(i -> RecordAnalysisServiceDto.builder()
            .category(i.get(record.category))
            .total(i.get(record.amount.longValue().sum()))
            .build()
        ).collect(Collectors.toList());
    }

}