package com.zerototen.savegame.domain.dto.request;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    private String content;

    public CreatePostServiceDto toServiceDto(Long challengeId){
        return CreatePostServiceDto.builder()
            .challengeId(challengeId)
            .content(this.getContent())
            .build();
    }
}
