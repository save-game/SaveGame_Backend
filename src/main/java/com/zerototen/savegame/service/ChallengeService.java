package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.security.TokenProvider;
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

        Challenge challenge = challengeRepository.save(Challenge.from(serviceDto));
        log.info("Create challenge -> challengeId: {}", challenge.getId());

        ChallengeMember challengeMember = ChallengeMember.builder()
            .challenge(challenge)
            .member(member)
            .ongoingYn(true)
            .build();

        challengeMemberRepository.save(challengeMember);
        log.info("Add challenge member -> challengeId: {}, memberId: {}", challenge.getId(), member.getId());

        return ResponseDto.success(challenge);
    }

}