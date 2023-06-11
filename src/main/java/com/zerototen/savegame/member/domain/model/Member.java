package com.zerototen.savegame.member.domain.model;

import com.zerototen.savegame.member.domain.controller.dto.SignUpForm;
import java.time.LocalDateTime;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditOverride;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class Member extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;
  private String password;
  private String nickname;
  private String imageUrl;
  private LocalDateTime deletedAt;

  public static Member from(SignUpForm form){
    return Member.builder()
        .email(form.getEmail().toLowerCase(Locale.ROOT))
        .password(form.getPassword())
        .nickname(form.getNickname())
        .imageUrl("default.png")
        .build();
  }
}
