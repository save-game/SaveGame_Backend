package com.zerototen.savegame.domain.dto.response;

import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Post;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private Long challengeId;
    private MemberResponse author;
    private String postContent;
    private List<Image> imageList;
    private int heartCnt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
            .id(post.getId())
            .challengeId(post.getChallenge().getId())
            .author(MemberResponse.from(post.getMember()))
            .postContent(post.getContent())
            .imageList(post.getImageList())
            .heartCnt(post.getHeartCnt())
            .build();
    }

}