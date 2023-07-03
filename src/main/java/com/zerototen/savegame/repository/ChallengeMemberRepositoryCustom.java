package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.dto.response.MemberChallengeResponse;
import com.zerototen.savegame.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeMemberRepositoryCustom {

    Page<MemberChallengeResponse> findChallengeListByMemberOrderByEndDate(Member member,
        Pageable pageable);

}