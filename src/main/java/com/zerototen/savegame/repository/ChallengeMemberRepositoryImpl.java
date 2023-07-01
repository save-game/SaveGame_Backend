package com.zerototen.savegame.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerototen.savegame.domain.dto.response.MemberChallengeResponse;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.QChallenge;
import com.zerototen.savegame.domain.entity.QChallengeMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengeMemberRepositoryImpl implements ChallengeMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberChallengeResponse> findChallengeListByMemberOrderByEndDate(Member member,
        Pageable pageable) {

        QChallengeMember challengeMember = QChallengeMember.challengeMember;

        QChallenge challenge = challengeMember.challenge;

        BooleanExpression condition = challengeMember.member.eq(member);

        List<MemberChallengeResponse> result = queryFactory
            .select(Projections.constructor(MemberChallengeResponse.class,
                challenge.id,
                challenge.title,
                challenge.endDate))
            .from(challengeMember)
            .where(condition)
            .orderBy(challenge.endDate.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long totalCount = queryFactory
            .select(challenge.id.count())
            .from(challengeMember)
            .where(condition)
            .fetchOne();

        return new PageImpl<>(result, pageable, totalCount != null ? totalCount : 0L);
    }

}