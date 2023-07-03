package com.zerototen.savegame.scheduler;

import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.ChallengeMemberResult;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.ChallengeMemberResultRepository;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.repository.RecordRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeResultScheduler {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeMemberResultRepository challengeMemberResultRepository;
    private final RecordRepository recordRepository;

    // 매일 0시마다 종료된 챌린지 작업
    @Transactional
    @Scheduled(cron = "${scheduler.challenge.result}", zone = "Asia/Seoul") // 매일 0시에 실행
    public void challengeResultScheduler() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("start challenge result scheduler -> endDate: {}", yesterday);
        // 종료일이 현재 전날인 챌린지 조회 (이전꺼는 미리 처리했다 가정)
        List<Challenge> challengeList = challengeRepository.findAllByEndDate(
            LocalDate.now().minusDays(1));

        // 챌린지가 없는 경우 종료
        if (challengeList.isEmpty()) {
            log.info("there is no challenge list to create -> endDate: {}", yesterday);
            return;
        }

        for (Challenge challenge : challengeList) {
            List<ChallengeMember> challengeMemberList = challengeMemberRepository.findAllByChallenge(
                challenge);

            List<ChallengeMemberResult> challengeMemberResultList = new ArrayList<>();

            for (ChallengeMember challengeMember : challengeMemberList) {
                if (challengeMember.isOngoingYn()) {
                    challengeMember.changeOngoingYn();
                }

                Member member = challengeMember.getMember();

                if (challengeMemberResultRepository.existsByChallengeMember(challengeMember)) {
                    log.warn("already exists challenge result -> challengeId: {}, memberId: {}",
                        challenge.getId(), member.getId());
                    continue;
                }

                Long total = recordRepository.findTotalByMemberAndChallenge(member, challenge)
                    .orElse(0L);

                log.info("create challenge result -> challengeId: {}, memberId: {}",
                    challenge.getId(), member.getId());
                challengeMemberResultList.add(ChallengeMemberResult.builder()
                    .challenge(challenge)
                    .challengeMember(challengeMember)
                    .totalAmount(total)
                    .build());
            }

            if(challengeMemberResultList.isEmpty()) {
                continue;
            }
            challengeMemberResultRepository.saveAll(challengeMemberResultList);
            log.info("save all challenge member result -> challengeId: {}", challenge.getId());
        }
        log.info("finish challenge result scheduler -> endDate: {}", yesterday);
    }

}