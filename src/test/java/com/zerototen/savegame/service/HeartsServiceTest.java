package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.repository.HeartsRepository;
import com.zerototen.savegame.repository.ImageRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeartsServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private HeartsRepository heartsRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private HeartsService heartsService;

    @Test
    @DisplayName("하트 활성화")
    void saveHearts() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

        Post post = getPost();
        willReturn(Optional.of(post)).given(postRepository).findById(any());
        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

        //when
        ResponseDto<?> responseDto = heartsService.saveHearts(request, post.getId());

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals("Heart saved!", responseDto.getData());

    }

    @Test
    @DisplayName("하트 활성화 중복")
    void twoTimesSaveHearts() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

        Post post = getPost();
        willReturn(Optional.of(post)).given(postRepository).findById(any());
        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
        willReturn(true)
            .given(heartsRepository).existsByMember_IdAndPost_Id(member.getId(),post.getId());

        //when
        ResponseDto<?> responseDto = heartsService.saveHearts(request, post.getId());

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals("Unheart processed!", responseDto.getData());
    }

    @Test
    @DisplayName("하트 비활성화")
    void unHearts() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

        Post post = getPost();
        willReturn(Optional.of(post)).given(postRepository).findById(any());
        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
        willReturn(true)
            .given(heartsRepository).existsByMember_IdAndPost_Id(member.getId(),post.getId());

        //when
        ResponseDto<?> responseDto = heartsService.unHearts(request, post.getId());

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals("Unheart processed!", responseDto.getData());
    }

    @Test
    @DisplayName("하트 비활성화 중복")
    void twoTimesUnHearts() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

        Post post = getPost();
        willReturn(Optional.of(post)).given(postRepository).findById(any());
        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
        willReturn(false)
            .given(heartsRepository).existsByMember_IdAndPost_Id(member.getId(),post.getId());

        //when
        ResponseDto<?> responseDto = heartsService.unHearts(request, post.getId());

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals("Heart saved!", responseDto.getData());
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

    private Post getPost() {
        return Post.builder()
            .id(1L)
            .build();
    }
}