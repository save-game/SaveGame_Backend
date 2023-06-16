package com.zerototen.savegame.domain.dto;

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

    public RecordAnalysisResponse toResponse() {
        return RecordAnalysisResponse.builder()
            .category(this.getCategory().getName())
            .total(this.getTotal())
            .build();
    }

}