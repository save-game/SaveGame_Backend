package com.zerototen.savegame.domain.entity;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.type.Category;
import java.time.LocalDate;
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
import org.springframework.lang.Nullable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE member SET deleted_at = CURRENT_TIMESTAMP WHERE challenge_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private int goalAmount;

    @Enumerated(EnumType.STRING)
    private Category category;

    private int maxPeople;
    private LocalDateTime deletedAt;

    public static Challenge from(CreateChallengeServiceDto serviceDto) {
        return Challenge.builder()
            .title(serviceDto.getTitle())
            .content(serviceDto.getContent())
            .startDate(serviceDto.getStartDate())
            .endDate(serviceDto.getEndDate())
            .goalAmount(serviceDto.getGoalAmount())
            .category(serviceDto.getCategory())
            .maxPeople(serviceDto.getMaxPeople())
            .build();
    }

}