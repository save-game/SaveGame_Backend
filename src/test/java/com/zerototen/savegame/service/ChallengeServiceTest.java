package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeMemberRepository challengeMemberRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private ChallengeService challengeService;

    @Test
    @DisplayName("챌린지 생성 성공")
    void testCreateSuccess() {
        //given
        CreateChallengeServiceDto serviceDto = getCreateChallengeServiceDto(Category.FOOD);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
        Challenge challenge = getChallenge(Category.FOOD, member.getId());

        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

        given(challengeRepository.save(any(Challenge.class)))
            .willReturn(challenge);

        ArgumentCaptor<Challenge> challengeArgumentCaptor = ArgumentCaptor.forClass(Challenge.class);
        ArgumentCaptor<ChallengeMember> challengeMemberArgumentCaptor = ArgumentCaptor.forClass(ChallengeMember.class);

        //when
        ResponseDto<?> responseDto = challengeService.create(request, serviceDto);

        //then
        then(challengeRepository).should().save(challengeArgumentCaptor.capture());
        then(challengeMemberRepository).should().save(challengeMemberArgumentCaptor.capture());
        assertTrue(responseDto.isSuccess());
        assertEquals(member.getId(), challengeArgumentCaptor.getValue().getCreateMemberId());
        assertEquals(serviceDto.getTitle(), challengeArgumentCaptor.getValue().getTitle());
        assertEquals(serviceDto.getContent(), challengeArgumentCaptor.getValue().getContent());
        assertEquals(serviceDto.getStartDate(), challengeArgumentCaptor.getValue().getStartDate());
        assertEquals(serviceDto.getEndDate(), challengeArgumentCaptor.getValue().getEndDate());
        assertEquals(serviceDto.getGoalAmount(), challengeArgumentCaptor.getValue().getGoalAmount());
        assertEquals(serviceDto.getCategory(), challengeArgumentCaptor.getValue().getCategory());
        assertEquals(serviceDto.getMaxPeople(), challengeArgumentCaptor.getValue().getMaxPeople());
    }

    @Nested
    @DisplayName("챌린지 참가")
    class testJoinChallenge {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Challenge challenge = getChallenge(Category.FOOD, member.getId());

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(challengeRepository.findById(anyLong()))
                .willReturn(Optional.of(challenge));

            given(challengeMemberRepository.existsByMemberAndChallenge(any(Member.class), any(Challenge.class)))
                .willReturn(Boolean.FALSE);

            given(challengeMemberRepository.countByChallenge(any(Challenge.class)))
                .willReturn(challenge.getMaxPeople() - 1);

            ArgumentCaptor<ChallengeMember> argumentCaptor = ArgumentCaptor.forClass(ChallengeMember.class);

            //when
            ResponseDto<?> responseDto = challengeService.join(request, 1L);

            //then
            then(challengeMemberRepository).should().save(argumentCaptor.capture());
            assertEquals(member, argumentCaptor.getValue().getMember());
            assertEquals(challenge, argumentCaptor.getValue().getChallenge());
            assertTrue(argumentCaptor.getValue().isOngoingYn());
            assertTrue(responseDto.isSuccess());
            assertEquals("Join Challenge Success", responseDto.getData());

        }

        @Test
        @DisplayName("실패 - 인원이 가득찬 챌린지")
        void fail_ChallengeIsFull() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Challenge challenge = getChallenge(Category.FOOD, member.getId());

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(challengeRepository.findById(anyLong()))
                .willReturn(Optional.of(challenge));

            given(challengeMemberRepository.existsByMemberAndChallenge(any(Member.class), any(Challenge.class)))
                .willReturn(Boolean.FALSE);

            given(challengeMemberRepository.countByChallenge(any(Challenge.class)))
                .willReturn(challenge.getMaxPeople());

            //when
            ResponseDto<?> responseDto = challengeService.join(request, 1L);

            //then
            then(challengeMemberRepository).should(never()).save(any());
            assertFalse(responseDto.isSuccess());
            assertEquals("인원이 다 찼습니다.", responseDto.getData());
        }
    }

    private Member getMember() {
        return Member.builder()
            .id(2L)
            .email("abc@gmail.com")
            .nickname("Nick")
            .password("1")
            .profileImageUrl("default.png")
            .build();
    }

    private static Challenge getChallenge(Category category, Long createMemberId) {
        return Challenge.builder()
            .id(1L)
            .createMemberId(createMemberId)
            .title("title")
            .content("content")
            .startDate(LocalDate.of(2023, 7, 1))
            .endDate(LocalDate.of(2023, 8, 1))
            .goalAmount(300000)
            .category(category)
            .maxPeople(10)
            .build();
    }

    private static CreateChallengeServiceDto getCreateChallengeServiceDto(Category category) {
        return CreateChallengeServiceDto.builder()
            .title("title")
            .content("content")
            .startDate(LocalDate.of(2023, 7, 1))
            .endDate(LocalDate.of(2023, 8, 1))
            .goalAmount(300000)
            .category(category)
            .maxPeople(10)
            .build();
    }

}