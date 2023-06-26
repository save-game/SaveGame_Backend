package com.zerototen.savegame.domain.dto;


import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Post;
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

    public Post toEntity() {
        return Post.builder()
            .challengeId(this.getChallengeId())
            .memberId(this.getMemberId())
            .content(this.getContent())
            .build();
    }

}
