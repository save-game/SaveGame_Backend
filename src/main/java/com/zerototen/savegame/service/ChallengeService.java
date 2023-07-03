package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.response.ChallengeMemberResponse;
import com.zerototen.savegame.domain.dto.response.ChallengeMemberResultResponse;
import com.zerototen.savegame.domain.dto.response.ChallengeRecordResponse;
import com.zerototen.savegame.domain.dto.response.ChallengeStatusResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.ChallengeMemberResult;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.SearchType;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.ChallengeMemberResultRepository;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.repository.RecordRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeMemberResultRepository challengeMemberResultRepository;
    private final RecordRepository recordRepository;
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

    public ResponseDto<?> getChallengeList(String keyword, String searchType, int minAmount,
        int maxAmount, String category, final Pageable pageable) {
        SearchType enumSearchType = SearchType.valueOf(searchType.toUpperCase(Locale.ROOT));
        Category enumCategory =
            category == null ? null : Category.valueOf(category.toUpperCase(Locale.ROOT));

        return ResponseDto.success(
            challengeRepository.findAllStartDateBeforeNowAndOptional(keyword, enumSearchType,
                minAmount, maxAmount, enumCategory, pageable));
    }

    @Transactional
    public ResponseDto<?> getChallengeStatus(Long challengeId) {

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElse(null);

        if (challenge == null) {
            return ResponseDto.fail("챌린지가 존재하지 않습니다.");
        }

        boolean isChallengeEnd = LocalDate.now().isAfter(challenge.getEndDate());

        // 챌린지 멤버 리스트
        List<ChallengeMember> challengeMemberList = challengeMemberRepository.findAllByChallenge(
            challenge);

        if (!isChallengeEnd) { // 진행 중인 챌린지
            List<ChallengeMemberResponse> challengeMemberResponseList = new ArrayList<>();

            for (ChallengeMember challengeMember : challengeMemberList) {
                List<ChallengeRecordResponse> recordList = new ArrayList<>();
                if (!challenge.getStartDate().isAfter(LocalDate.now())) { // 챌린지 시작 후
                    // 챌린지 멤버의 지출 리스트
                    recordList = recordRepository.findTotalAndUseDateByMemberAndChallengeGroupByUseDate(
                        challengeMember.getMember(), challenge);

                    long total = recordList.stream().mapToLong(ChallengeRecordResponse::getAmount)
                        .sum();
                    if (total > challenge.getGoalAmount()) { // 목표금액을 넘긴 경우 onGoingYn false
                        if (challengeMember.isOngoingYn()) {
                            challengeMember.changeOngoingYn();
                        }
                    } else { // 지출 수정 때문에 목표금액 이하로 바뀔 수 있으므로
                        if (!challengeMember.isOngoingYn()) {
                            challengeMember.changeOngoingYn();
                        }
                    }
                }
                challengeMemberResponseList.add(ChallengeMemberResponse.builder()
                    .memberId(challengeMember.getMember().getId())
                    .nickname(challengeMember.getMember().getNickname())
                    .status(challengeMember.isOngoingYn() ? 1 : 0)
                    .recordList(recordList)
                    .build());
            }

            return ResponseDto.success(ChallengeStatusResponse.builder()
                .title(challenge.getTitle())
                .content(challenge.getContent())
                .challengeStatus(1)
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .goalAmount(challenge.getGoalAmount())
                .category(challenge.getCategory())
                .challengeMemberList(challengeMemberResponseList)
                .build());

        } else { // 종료된 챌린지
            List<ChallengeMemberResult> challengeMemberResultList =
                challengeMemberResultRepository.findAllByChallengeOrderByTotalAmountAsc(challenge);

            if (challengeMemberResultList.isEmpty()) {
                return ResponseDto.fail("챌린지 종료 작업이 완료되지 않았습니다.");
            }

            List<ChallengeMemberResultResponse> challengeMemberResultResponseList = new ArrayList<>();

            for (ChallengeMemberResult challengeMemberResult : challengeMemberResultList) {
                Member member = challengeMemberResult.getChallengeMember().getMember();
                long total = challengeMemberResult.getTotalAmount();
                challengeMemberResultResponseList.add(ChallengeMemberResultResponse.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .status(total > challenge.getGoalAmount() ? 0 : 1)
                    .totalAmount(total)
                    .build());
            }

            return ResponseDto.success(ChallengeStatusResponse.builder()
                .title(challenge.getTitle())
                .content(challenge.getContent())
                .challengeStatus(0)
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .goalAmount(challenge.getGoalAmount())
                .category(challenge.getCategory())
                .challengeMemberList(challengeMemberResultResponseList)
                .build());
        }
    }

}