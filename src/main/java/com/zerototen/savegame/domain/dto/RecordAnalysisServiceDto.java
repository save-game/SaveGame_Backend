package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.dto.response.RecordAnalysisResponse;
import com.zerototen.savegame.domain.type.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordAnalysisServiceDto {

    private Category category;
    private Long total;

}