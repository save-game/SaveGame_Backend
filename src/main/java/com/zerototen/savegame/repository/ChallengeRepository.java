package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Challenge;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long>,
    ChallengeRepositoryCustom {

    List<Challenge> findAllByEndDate(LocalDate endDate);

}