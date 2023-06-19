package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
}