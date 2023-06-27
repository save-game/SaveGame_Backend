package com.zerototen.savegame.domain.dto;


import com.zerototen.savegame.domain.dto.request.CreatePostRequest;
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
public class CreatePostServiceDto {

    private Long challengeId;
    private Long memberId;
    private String content;

    public static CreatePostServiceDto of(CreatePostRequest request, Long challengeId){
        return CreatePostServiceDto.builder()
            .challengeId(challengeId)
            .content(request.getContent())
            .build();
    }

}