package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.HeartRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private HeartRepository heartRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private HeartService heartsService;


    @Nested
    @DisplayName("성공")
    class Success {
        @Test
        @DisplayName("하트 생성")
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
            ResponseDto<?> responseDto = heartsService.create(request, post.getId());

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Heart Create Success", responseDto.getData());

        }

        @Test
        @DisplayName("하트 삭제")
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
                .given(heartRepository).existsByMemberAndPost(member, post);

            //when
            ResponseDto<?> responseDto = heartsService.delete(request, post.getId());

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Heart Delete Success", responseDto.getData());
        }
    }

    @Nested
    @DisplayName("실패")
    class Fail {
        @Test
        @DisplayName("하트 생성_포스트가 없음")
        void heartNotFoundPost() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            Post post = getPost();
            willReturn(Optional.empty()).given(postRepository).findById(1L);
            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            //when
            CustomException exception = assertThrows(CustomException.class,
                ()->heartsService.create(request, post.getId()));

            //then
            assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
        }

        @Test
        @DisplayName("하트삭제_포스트가 없음")
        void unHeartNotFoundPost() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            Post post = getPost();
            willReturn(Optional.empty()).given(postRepository).findById(1L);
            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            //when
            CustomException exception = assertThrows(CustomException.class,
                ()->heartsService.delete(request, post.getId()));

            //then
            assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
        }

        @Test
        @DisplayName("하트 삭제 중복")
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
                .given(heartRepository).existsByMemberAndPost(member, post);

            //when
            CustomException exception = assertThrows(CustomException.class,
                () -> heartsService.delete(request, post.getId()));

            //then
            assertEquals(ErrorCode.NOT_FOUND_HEART, exception.getErrorCode());
        }

        @Test
        @DisplayName("하트 생성 중복")
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
                .given(heartRepository).existsByMemberAndPost(member, post);

            //when
            CustomException exception = assertThrows(CustomException.class,
                () -> heartsService.create(request, post.getId()));

            //then
            assertEquals(ErrorCode.ALREADY_REGISTERED_HEART, exception.getErrorCode());
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

    private Post getPost() {
        return Post.builder()
            .id(1L)
            .build();
    }

}