package com.zerototen.savegame.domain.dto;

import com.zerototen.savegame.repository.MemberRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@ToString
@Getter
public class PostDetailDto {

    private final MemberRepository memberRepository;

    private Long postId;
    private String nickname;
    private String profileImage;
    private String content;
    private List<String> urlImages;
    private Long heartCount;
    private boolean heartState;


}
