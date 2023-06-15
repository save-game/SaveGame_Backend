package com.zerototen.savegame.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerototen.savegame.domain.QRecord;
import com.zerototen.savegame.domain.Record;
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

}