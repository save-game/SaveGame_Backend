package com.zerototen.savegame.domain.dto.response;

import com.zerototen.savegame.domain.entity.Image;
import java.time.LocalDateTime;
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
    private boolean hasHeart;
    private LocalDateTime createdAt;

}