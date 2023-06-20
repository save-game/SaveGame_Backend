package com.zerototen.savegame.domain.entity;

import com.zerototen.savegame.domain.dto.SignupInfoDto;
import com.zerototen.savegame.domain.type.Authority;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Authority userRole;

    public Member(SignupInfoDto signupInfoDto) {
        this.email = signupInfoDto.getEmail();
        this.nickname = signupInfoDto.getNickname();
        this.profileImageUrl = signupInfoDto.getImgUrl();
        this.password = "@";
        this.userRole = signupInfoDto.getRole();
    }

}