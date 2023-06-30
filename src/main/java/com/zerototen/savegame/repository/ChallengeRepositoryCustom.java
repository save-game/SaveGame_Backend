package com.zerototen.savegame.repository;

import com.zerototen.savegame.domain.dto.response.ChallengeSearchResponse;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeRepositoryCustom {

    Page<ChallengeSearchResponse> findAllStartDateBeforeNowAndOptional(String keyword,
        SearchType searchType, Integer min, Integer max, Category category, Pageable pageable);

}