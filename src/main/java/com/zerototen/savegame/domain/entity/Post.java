package com.zerototen.savegame.domain.entity;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Long challengeId;
    private Long memberId;
    private String content;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> imageList;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Heart> heartList;

    public void update(UpdatePostServiceDto serviceDto){
        this.content = serviceDto.getComment();
    }

    public static Post from(CreatePostServiceDto dto) {
        return Post.builder()
            .challengeId(dto.getChallengeId())
            .memberId(dto.getMemberId())
            .content(dto.getContent())
            .build();
    }

}