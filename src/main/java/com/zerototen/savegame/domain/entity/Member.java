package com.zerototen.savegame.domain.entity;

import com.zerototen.savegame.domain.dto.SignupInfoDto;
import com.zerototen.savegame.domain.dto.request.UpdateNicknameRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePasswordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateProfileImageUrlRequest;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE member SET deleted_at = CURRENT_TIMESTAMP WHERE member_id = ?")
@Where(clause = "deleted_at IS NULL")
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

    public void updatePassword(UpdatePasswordRequest request) {
        this.password = new BCryptPasswordEncoder().encode(request.getNewPassword());
    }

    public void updateNickname(UpdateNicknameRequest request) {
        this.nickname = request.getNickname();
    }

    public void updateProfileImageUrl(UpdateProfileImageUrlRequest request) {
        this.profileImageUrl = request.getProfileImageUrl();
    }

}