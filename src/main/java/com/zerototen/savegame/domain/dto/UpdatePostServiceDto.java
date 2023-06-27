package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.domain.dto.request.UpdatePostRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostServiceDto {

    private Long id;
    private String comment;

    public static UpdatePostServiceDto of(Long postId, UpdatePostRequest request){
        return UpdatePostServiceDto.builder()
            .id(postId)
            .comment(request.getContent())
            .build();
    }

}