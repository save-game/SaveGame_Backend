package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, Long>,
    ChallengeMemberRepositoryCustom {

    Optional<ChallengeMember> findByMemberAndChallenge(Member member, Challenge challenge);

    boolean existsByMemberAndChallenge(Member member, Challenge challenge);

    int countByChallenge(Challenge challenge);

}