package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByChallengeIdOrderByIdDesc(Long challenge_id, Pageable pageable);

}
