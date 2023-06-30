package com.zerototen.savegame.domain.dto;


import com.zerototen.savegame.domain.dto.request.CreatePostRequest;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostServiceDto {

    private String content;
    private List<String> imageUrlList;

    public static CreatePostServiceDto from(CreatePostRequest request) {
        return CreatePostServiceDto.builder()
            .content(request.getContent())
            .imageUrlList(request.getImageUrlList())
            .build();
    }

}