package com.zerototen.savegame.domain.entity;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> imageList;

    private int heartCnt;

    public void update(UpdatePostServiceDto serviceDto) {
        this.content = serviceDto.getComment();
    }

    public static Post of(CreatePostServiceDto dto, Member member, Challenge challenge) {
        return Post.builder()
                .challenge(challenge)
                .member(member)
                .content(dto.getContent())
                .heartCnt(0)
                .build();
    }

    public void plusHeart() {
        this.heartCnt++;
    }

    public void minusHeart() {
        if (heartCnt != 0) {
            this.heartCnt--;
        }
    }

}