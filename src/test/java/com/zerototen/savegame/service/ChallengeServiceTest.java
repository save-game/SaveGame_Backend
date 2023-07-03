package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.response.ChallengeSearchResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.ChallengeMember;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.SearchType;
import com.zerototen.savegame.repository.ChallengeMemberRepository;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        ArgumentCaptor<Challenge> challengeArgumentCaptor = ArgumentCaptor.forClass(
            Challenge.class);
        ArgumentCaptor<ChallengeMember> challengeMemberArgumentCaptor = ArgumentCaptor.forClass(
            ChallengeMember.class);

        //when
        ResponseDto<?> responseDto = challengeService.create(request, serviceDto);

        //then
        then(challengeRepository).should().save(challengeArgumentCaptor.capture());
        then(challengeMemberRepository).should().save(challengeMemberArgumentCaptor.capture());
        assertTrue(responseDto.isSuccess());
        assertEquals(member.getId(), challengeArgumentCaptor.getValue().getMasterMemberId());
        assertEquals(serviceDto.getTitle(), challengeArgumentCaptor.getValue().getTitle());
        assertEquals(serviceDto.getContent(),
            challengeArgumentCaptor.getValue().getContent());
        assertEquals(serviceDto.getStartDate(), challengeArgumentCaptor.getValue().getStartDate());
        assertEquals(serviceDto.getEndDate(), challengeArgumentCaptor.getValue().getEndDate());
        assertEquals(serviceDto.getGoalAmount(),
            challengeArgumentCaptor.getValue().getGoalAmount());
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

            given(challengeMemberRepository.existsByMemberAndChallenge(any(Member.class),
                any(Challenge.class)))
                .willReturn(Boolean.FALSE);

            given(challengeMemberRepository.countByChallenge(any(Challenge.class)))
                .willReturn(challenge.getMaxPeople() - 1);

            ArgumentCaptor<ChallengeMember> argumentCaptor = ArgumentCaptor.forClass(
                ChallengeMember.class);

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

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("인원이 가득찬 챌린지")
            void challengeIsFull() {
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                Challenge challenge = getChallenge(Category.FOOD, member.getId());

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(challengeRepository.findById(anyLong()))
                    .willReturn(Optional.of(challenge));

                given(challengeMemberRepository.existsByMemberAndChallenge(any(Member.class),
                    any(Challenge.class)))
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

            @Test
            @DisplayName("이미 시작된 챌린지")
            void alreadyStartedChallenge() {
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                Challenge challenge = getChallenge(Category.FOOD, member.getId());
                challenge.setStartDate(LocalDate.now());

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(challengeRepository.findById(anyLong()))
                    .willReturn(Optional.of(challenge));

                given(challengeMemberRepository.existsByMemberAndChallenge(any(Member.class),
                    any(Challenge.class)))
                    .willReturn(Boolean.FALSE);

                //when
                ResponseDto<?> responseDto = challengeService.join(request, 1L);

                //then
                then(challengeMemberRepository).should(never()).save(any());
                assertFalse(responseDto.isSuccess());
                assertEquals("이미 시작된 챌린지입니다.", responseDto.getData());
            }
        }
    }

    @Nested
    @DisplayName("챌린지 나가기")
    class testExitChallenge {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Challenge challenge = getChallenge(Category.FOOD, 1L);
            ChallengeMember challengeMember = getChallengeMember(member, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(challengeRepository.findById(anyLong()))
                .willReturn(Optional.of(challenge));

            given(challengeMemberRepository.findByMemberAndChallenge(any(Member.class),
                any(Challenge.class)))
                .willReturn(Optional.of(challengeMember));

            ArgumentCaptor<ChallengeMember> argumentCaptor = ArgumentCaptor.forClass(
                ChallengeMember.class);

            //when
            ResponseDto<?> responseDto = challengeService.exit(request, 1L);

            //then
            then(challengeMemberRepository).should().delete(argumentCaptor.capture());
            assertTrue(responseDto.isSuccess());
            assertEquals("Exit Challenge Success", responseDto.getData());
        }

        @Test
        @DisplayName("실패 - 이미 시작된 챌린지")
        void fail_AlreadyStartedChallenge() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Challenge challenge = getChallenge(Category.FOOD, 1L);
            challenge.setStartDate(LocalDate.now());
            challenge.setEndDate(LocalDate.now().plusDays(1));
            ChallengeMember challengeMember = getChallengeMember(member, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(challengeRepository.findById(anyLong()))
                .willReturn(Optional.of(challenge));

            given(challengeMemberRepository.findByMemberAndChallenge(any(Member.class),
                any(Challenge.class)))
                .willReturn(Optional.of(challengeMember));

            //when
            ResponseDto<?> responseDto = challengeService.exit(request, 1L);

            //then
            then(challengeMemberRepository).should(never()).delete(any(ChallengeMember.class));
            assertFalse(responseDto.isSuccess());
            assertEquals("이미 시작된 챌린지입니다.", responseDto.getData());
        }

    }

    @Nested
    @DisplayName("챌린지 검색 - 성공")
    class testChallengeSearchSuccess {

        @Test
        @DisplayName("빈 Keyword")
        void keywordIsEmpty() {
            //given
            Pageable pageable = PageRequest.of(0, 3);
            Page<ChallengeSearchResponse> result = getSearchResult("", SearchType.ALL, 10000,
                50000, pageable);
            given(challengeRepository.findAllStartDateBeforeNowAndOptional(anyString(),
                any(SearchType.class),
                anyInt(), anyInt(), isNull(), any(Pageable.class)))
                .willReturn(result);

            //when
            ResponseDto<?> responseDto = challengeService.getChallengeList("", "ALL", 10000,
                50000, null, pageable);
            Page<ChallengeSearchResponse> response = (Page<ChallengeSearchResponse>) responseDto.getData();
            //then
            assertTrue(responseDto.isSuccess());
            assertEquals(pageable.getPageNumber(), response.getPageable().getPageNumber());
            assertEquals(pageable.getPageSize(), response.getPageable().getPageSize());
            int size = response.getPageable().getPageSize();
            for (int i = size; i > 0; i--) {
                assertEquals(i, response.getContent().get(size - i).getChallengeId());
                assertEquals("title" + i, response.getContent().get(size - i).getTitle());
                assertEquals(i + "content",
                    response.getContent().get(size - i).getChallengeContent());
                assertEquals(30000, response.getContent().get(size - i).getGoalAmount());
                assertEquals(LocalDate.now().plusDays(i),
                    response.getContent().get(size - i).getStartDate());
                assertEquals((i % 10) + 1, response.getContent().get(size - i).getCnt());
            }
        }

        @Test
        @DisplayName("Category 미입력")
        void inputExceptCategory() {
            //given
            Pageable pageable = PageRequest.of(0, 3);
            Page<ChallengeSearchResponse> result = getSearchResult("키워드", SearchType.ALL, 10000,
                50000,
                pageable);
            given(challengeRepository.findAllStartDateBeforeNowAndOptional(anyString(),
                any(SearchType.class), anyInt(), anyInt(), isNull(),
                any(Pageable.class)))
                .willReturn(result);

            //when
            ResponseDto<?> responseDto = challengeService.getChallengeList("키워드", "ALL", 10000,
                50000,
                null, pageable);
            Page<ChallengeSearchResponse> response = (Page<ChallengeSearchResponse>) responseDto.getData();
            //then
            assertTrue(responseDto.isSuccess());
            assertEquals(pageable.getPageNumber(), response.getPageable().getPageNumber());
            assertEquals(pageable.getPageSize(), response.getPageable().getPageSize());
            int size = response.getPageable().getPageSize();
            for (int i = size; i > 0; i--) {
                assertEquals(i, response.getContent().get(size - i).getChallengeId());
                assertEquals("키워드" + i, response.getContent().get(size - i).getTitle());
                assertEquals(i + "키워드",
                    response.getContent().get(size - i).getChallengeContent());
                assertEquals(30000, response.getContent().get(size - i).getGoalAmount());
                assertEquals(LocalDate.now().plusDays(i),
                    response.getContent().get(size - i).getStartDate());
                assertEquals((i % 10) + 1, response.getContent().get(size - i).getCnt());
            }
        }

        @Test
        @DisplayName("모두 입력")
        void inputAll() {
            //given
            Pageable pageable = PageRequest.of(0, 3);
            Page<ChallengeSearchResponse> result = getSearchResult("부제",
                SearchType.CONTENT, 30000, 70000,
                pageable);
            given(challengeRepository.findAllStartDateBeforeNowAndOptional(anyString(),
                any(SearchType.class), anyInt(), anyInt(), any(Category.class),
                any(Pageable.class)))
                .willReturn(result);

            //when
            ResponseDto<?> responseDto = challengeService.getChallengeList("부제",
                "CONTENT", 30000, 70000, "ALL", pageable);
            Page<ChallengeSearchResponse> response = (Page<ChallengeSearchResponse>) responseDto.getData();
            //then
            assertTrue(responseDto.isSuccess());
            assertEquals(pageable.getPageNumber(), response.getPageable().getPageNumber());
            assertEquals(pageable.getPageSize(), response.getPageable().getPageSize());
            int size = response.getPageable().getPageSize();
            for (int i = size; i > 0; i--) {
                assertEquals(i, response.getContent().get(size - i).getChallengeId());
                assertEquals("title" + i, response.getContent().get(size - i).getTitle());
                assertEquals(i + "부제", response.getContent().get(size - i).getChallengeContent());
                assertEquals(50000, response.getContent().get(size - i).getGoalAmount());
                assertEquals(LocalDate.now().plusDays(i),
                    response.getContent().get(size - i).getStartDate());
                assertEquals((i % 10) + 1, response.getContent().get(size - i).getCnt());
            }
        }
    }

    private static Member getMember() {
        return Member.builder()
            .id(2L)
            .email("abc@gmail.com")
            .nickname("Nick")
            .password("1")
            .profileImageUrl("default.png")
            .build();
    }

    private static ChallengeMember getChallengeMember(Member member, Challenge challenge) {
        return ChallengeMember.builder()
            .id(1L)
            .member(member)
            .challenge(challenge)
            .ongoingYn(true)
            .build();
    }

    private static Challenge getChallenge(Category category, Long masterMemberId) {
        return Challenge.builder()
            .id(1L)
            .masterMemberId(masterMemberId)
            .title("title")
            .content("content")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(1).plusMonths(1))
            .goalAmount(300000)
            .category(category)
            .maxPeople(10)
            .build();
    }

    private static CreateChallengeServiceDto getCreateChallengeServiceDto(Category category) {
        return CreateChallengeServiceDto.builder()
            .title("title")
            .content("content")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(1).plusMonths(1))
            .goalAmount(300000)
            .category(category)
            .maxPeople(10)
            .build();
    }

    private static Page<ChallengeSearchResponse> getSearchResult(String keyword,
        SearchType searchType, int minAmount, int maxAmount, Pageable pageable) {

        String title =
            (keyword != null && searchType != null && !keyword.isEmpty())
                && !searchType.equals(SearchType.CONTENT) ? keyword : "title";
        String challengeContent =
            (keyword != null && searchType != null && !keyword.isEmpty())
                && !searchType.equals(SearchType.TITLE) ? keyword : "content";

        List<ChallengeSearchResponse> searchResultList = new ArrayList<>();
        for (int i = pageable.getPageSize(); i > 0; i--) {
            ChallengeSearchResponse searchResult = ChallengeSearchResponse.builder()
                .challengeId((long) i)
                .title(title + i)
                .challengeContent(i + challengeContent)
                .goalAmount((minAmount + maxAmount) / 2)
                .startDate(LocalDate.now().plusDays(i))
                .cnt((i % 10) + 1)
                .build();
            searchResultList.add(searchResult);
        }

        return new PageImpl<>(searchResultList, pageable, searchResultList.size());
    }

}