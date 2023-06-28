package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.Member;
import java.util.List;

public interface ChallengeMemberRepositoryCustom {

    List<Challenge> findChallengeListByMemberOrderByEndDate(Member member);

}