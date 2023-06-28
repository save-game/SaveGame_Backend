package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Heart;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {

    long countByPost_Id(long postId);
    boolean existsByMemberAndPost(Member member, Post post);
    void deleteByMemberAndPost(Member member, Post post);

}