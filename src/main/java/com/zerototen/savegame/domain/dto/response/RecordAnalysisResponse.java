package com.zerototen.savegame.domain.dto.response;

import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordAnalysisResponse {

    private String category;
    private long total;

    public static RecordAnalysisResponse from(RecordAnalysisServiceDto serviceDto) {
        return RecordAnalysisResponse.builder()
            .category(serviceDto.getCategory().getName())
            .total(serviceDto.getTotal())
            .build();
    }

}