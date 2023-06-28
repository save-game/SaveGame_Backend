package com.zerototen.savegame.domain.dto;


import com.zerototen.savegame.domain.dto.request.CreatePostRequest;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostServiceDto {

    private String content;

    public static CreatePostServiceDto from(CreatePostRequest request) {
        return CreatePostServiceDto.builder()
                .content(request.getContent())
                .build();
    }

}