package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.ChallengeMemberResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeMemberResultRepository extends
    JpaRepository<ChallengeMemberResult, Long> {

    List<ChallengeMemberResult> findAllByChallengeOrderByTotalAmountAsc(Challenge challenge);

    boolean existsByChallengeMember(ChallengeMember challengeMember);

}