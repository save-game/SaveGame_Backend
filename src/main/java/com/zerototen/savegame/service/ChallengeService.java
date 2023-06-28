package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public ResponseDto<?> create(HttpServletRequest request, CreateChallengeServiceDto serviceDto) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        Challenge challenge = challengeRepository.save(Challenge.of(serviceDto, member.getId()));
        log.info("Create challenge -> challengeId: {}, createMemberId: {}", challenge.getId(),
            challenge.getMasterMemberId());

        ChallengeMember challengeMember = ChallengeMember.builder()
            .challenge(challenge)
            .member(member)
            .ongoingYn(true)
            .build();

        challengeMemberRepository.save(challengeMember);
        log.info("Add challenge member -> challengeId: {}, memberId: {}", challenge.getId(),
            member.getId());

        return ResponseDto.success(challenge);
    }

    @Transactional
    public ResponseDto<?> join(HttpServletRequest request, Long challengeId) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElse(null);
        if (challenge == null) {
            return ResponseDto.fail("챌린지가 존재하지 않습니다.");
        }

        if (challengeMemberRepository.existsByMemberAndChallenge(member, challenge)) {
            return ResponseDto.fail("이미 참가한 챌린지입니다.");
        }

        if (challenge.getStartDate().minusDays(1).isBefore(LocalDate.now())) { // 시작일이 오늘과 같거나 이전이면
            return ResponseDto.fail("이미 시작된 챌린지입니다.");
        }

        if (challengeMemberRepository.countByChallenge(challenge) >= challenge.getMaxPeople()) {
            return ResponseDto.fail("인원이 다 찼습니다.");
        }

        ChallengeMember challengeMember = ChallengeMember.builder()
            .member(member)
            .challenge(challenge)
            .ongoingYn(true)
            .build();

        challengeMemberRepository.save(challengeMember);
        log.info("Add challenge member -> challengeId: {}, memberId: {}", challengeId,
            member.getId());
        return ResponseDto.success("Join Challenge Success");
    }

    @Transactional
    public ResponseDto<?> exit(HttpServletRequest request, Long challengeId) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElse(null);
        if (challenge == null) {
            return ResponseDto.fail("챌린지가 존재하지 않습니다.");
        }

        ChallengeMember challengeMember = challengeMemberRepository.findByMemberAndChallenge(member,
                challenge)
            .orElse(null);

        if (challengeMember == null) {
            return ResponseDto.fail("참가하지 않은 챌린지입니다.");
        }

        if (member.getId().equals(challenge.getMasterMemberId())) {
            return ResponseDto.fail("챌린지를 생성한 사용자는 나갈 수 없습니다.");
        }

        if (challenge.getStartDate().minusDays(1).isBefore(LocalDate.now())) { // 시작일이 오늘과 같거나 이전이면
            return ResponseDto.fail("이미 시작된 챌린지입니다.");
        }

        challengeMemberRepository.delete(challengeMember);
        log.info("Exit Challenge -> {}, memberId: {}", challengeId, member.getId());
        return ResponseDto.success("Exit Challenge Success");
    }

}