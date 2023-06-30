package com.zerototen.savegame.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerototen.savegame.domain.dto.response.ChallengeSearchResponse;
import com.zerototen.savegame.domain.entity.QChallenge;
import com.zerototen.savegame.domain.entity.QChallengeMember;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.SearchType;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ChallengeSearchResponse> findAllStartDateBeforeNowAndOptional(
        String keyword, SearchType searchType, Integer min, Integer max, Category category, // 필수 아님
        Pageable pageable) { // 필수
        QChallenge challenge = QChallenge.challenge;
        QChallengeMember challengeMember = QChallengeMember.challengeMember;

        BooleanExpression condition = challenge.startDate.after(LocalDate.now());

        // 검색어 & 검색타입 있는 경우
        if (keyword != null && searchType != null) {
            String search = "%" + keyword + "%";
            if (searchType.equals(SearchType.TITLE)) {
                condition = condition.and(challenge.title.like(search));
            } else if (searchType.equals(SearchType.CONTENT)) {
                condition = condition.and(challenge.content.like(search));
            } else {
                condition = condition.and(
                    challenge.title.like(search).or(challenge.content.like(search)));
            }
        }

        // 금액이 있는 경우
        if (min != null && max != null) {
            condition = condition.and(challenge.goalAmount.between(min, max));
        }

        // 카테고리 있는 경우
        if (category != null) {
            condition = condition.and(challenge.category.eq(category));
        }

        List<ChallengeSearchResponse> results = queryFactory
            .select(Projections.constructor(ChallengeSearchResponse.class,
                challenge.id,
                challenge.title,
                challenge.content,
                challenge.goalAmount,
                challenge.startDate,
                challenge.maxPeople,
                challengeMember.id.count().intValue()))
            .from(challenge)
            .leftJoin(challengeMember)
            .on(challenge.id.eq(challengeMember.challenge.id))
            .where(condition)
            .groupBy(challenge.id, challenge.title, challenge.content,
                challenge.goalAmount,
                challenge.startDate, challenge.maxPeople)
            .orderBy(challenge.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long totalCount = queryFactory
            .select(challenge.id.count())
            .from(challenge)
            .leftJoin(challengeMember)
            .on(challenge.id.eq(challengeMember.challenge.id))
            .where(condition)
            .fetchOne();

        return new PageImpl<>(results, pageable, totalCount != null ? totalCount : 0L);
    }

}