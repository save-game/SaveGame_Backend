package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Hearts;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartsRepository extends JpaRepository<Hearts, Long> {
    long countByPost_Id(long postId);
    boolean existsByMember_IdAndPost_Id(long memberId, long postId);
    void deleteByMemberAndPost(Member member, Post post);
}
