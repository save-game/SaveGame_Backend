package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long>, RecordRepositoryCustom {

}