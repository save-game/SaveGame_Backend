package com.zerototen.savegame.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.QChallengeMember;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengeMemberRepositoryImpl implements ChallengeMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Challenge> findChallengeListByMemberOrderByEndDate(Member member) {

        QChallengeMember challengeMember = QChallengeMember.challengeMember;

        BooleanExpression condition = challengeMember.member.eq(member)
            .and(challengeMember.challenge.endDate.after(
                LocalDate.now().minusDays(1))); // 종료된 챌린지는 조회하지 않음

        return new ArrayList<>(queryFactory
            .select(challengeMember.challenge)
            .from(challengeMember)
            .where(condition)
            .orderBy(challengeMember.challenge.endDate.asc())
            .fetch());
    }

}