package com.zerototen.savegame.service;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.repository.HeartRepository;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    TokenProvider tokenProvider;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private HeartRepository heartRepository;

    @InjectMocks
    private PostService postService;

    @Nested
    @DisplayName("성공")
    class Success {

        @Test
        @DisplayName("5 페이지 이상 포스트 조회 - next True")
        void postListMoreThan5Pages() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Pageable pageable = Pageable.ofSize(5);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            List<Post> posts = getPosts(13, member, challenge);
            Long size = Long.valueOf(posts.size());

            Page<Post> postPage = new PageImpl<>(posts, pageable, size);

            willReturn(postPage)
                .given(postRepository).findByChallengeIdOrderByIdDesc(anyLong(), any());

            //when
            ResponseDto<?> responseDto = postService.getPostList(request, 1L, pageable);
            Page<Post> all = postRepository.findByChallengeIdOrderByIdDesc(1L, pageable);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals(13, all.getNumberOfElements());
            assertTrue(all.hasNext());
        }

        @Test
        @DisplayName("5 페이지 이하 포스트 조회 - next False")
        void postListMoreLessThan5Pages() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Pageable pageable = Pageable.ofSize(5);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            List<Post> posts = getPosts(4, member, challenge);
            Long size = Long.valueOf(posts.size());

            Page<Post> postPage = new PageImpl<>(posts, pageable, size);

            willReturn(postPage)
                .given(postRepository).findByChallengeIdOrderByIdDesc(anyLong(), any());

            //when
            ResponseDto<?> responseDto = postService.getPostList(request, 1L, pageable);
            Page<Post> all = postRepository.findByChallengeIdOrderByIdDesc(1L, pageable);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals(4, all.getNumberOfElements());
            assertFalse(all.hasNext());
        }

        @Test
        @DisplayName("포스트 작성")
        void create() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            Post post = getPost();
            CreatePostServiceDto serviceDto = getCreateServiceDto();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
            given(challengeRepository.findById(anyLong()))
                .willReturn(Optional.of(challenge));
            given(postRepository.save(any()))
                .willReturn(post);

            ArgumentCaptor<Post> argumentCaptor = ArgumentCaptor.forClass(Post.class);

            //when
            ResponseDto<?> responseDto = postService.create(serviceDto, challenge.getId(), request);

            //then
            assertTrue(responseDto.isSuccess());
            verify(postRepository).save(argumentCaptor.capture());
            Post saved = argumentCaptor.getValue();
            assertEquals(2L, saved.getMember().getId());
            assertEquals("내용", saved.getContent());
        }

        @Test
        @DisplayName("포스트 수정")
        void update() {
            //given
            UpdatePostServiceDto serviceDto = getUpdateServiceDto();
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Post post = getPost(member, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            willReturn(Optional.of(post)).given(postRepository).findById(any());

            //when
            ResponseDto<?> responseDto = postService.update(request, serviceDto);
            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

            //then
            assertTrue(responseDto.isSuccess());
            verify(postRepository, times(1)).save(captor.capture());
            assertEquals("수정내용", captor.getValue().getContent());
        }

        @Test
        @DisplayName("포스트 삭제")
        void delete() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Post post = getPost(member, challenge);

            willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            willReturn(Optional.of(post)).given(postRepository).findById(1L);

            //when
            ResponseDto<?> responseDto = postService.delete(request, 1L);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Delete Success", responseDto.getData());
            verify(postRepository).delete(post);

        }
    }

    @Nested
    @DisplayName("실패")
    class Fail {
        @Test
        @DisplayName("포스트 생성 시, 챌린지가 존재하지 않음")
        void notFoundChallenge() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            Post post = getPost(member, challenge);
            CreatePostServiceDto serviceDto = getCreateServiceDto();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
            given(challengeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

            //when
            ResponseDto<?> responseDto = postService.create(serviceDto, challenge.getId(), request);

            //then
            assertFalse(responseDto.isSuccess());
            assertEquals("챌린지가 존재하지 않습니다.", responseDto.getData());
            verify(postRepository, never()).save(post);
        }

        @Test
        @DisplayName("포스트 수정 시, 포스트가 존재하지 않음")
        void notFoundPost_Update() {
            UpdatePostServiceDto serviceDto = getUpdateServiceDto();
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Post post = getPost(member, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            willReturn(Optional.empty()).given(postRepository).findById(any());

            //when
            ResponseDto<?> responseDto = postService.update(request, serviceDto);

            //then
            assertFalse(responseDto.isSuccess());
            assertEquals("게시물을 찾을 수 없습니다", responseDto.getData());
            verify(postRepository, never()).save(post);
        }

        @Test
        @DisplayName("포스트 수정 시, 일치하지 않는 사용자")
        void notMatchMember_Update() {
            UpdatePostServiceDto serviceDto = getUpdateServiceDto();
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member postMember = getMember();
            Member requestMember = getMember();
            postMember.setId(3L);
            Challenge challenge = getChallenge(null, postMember.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(requestMember);
            Post post = getPost(postMember, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
            willReturn(Optional.of(post)).given(postRepository).findById(any());

            //when
            ResponseDto<?> responseDto = postService.update(request, serviceDto);

            //then
            assertFalse(responseDto.isSuccess());
            assertEquals("해당 내역이 사용자의 내역이 아닙니다", responseDto.getData());
            verify(postRepository, never()).save(post);
        }

        @Test
        @DisplayName("포스트 삭제 시, 포스트가 존재하지 않음")
        void notFoundPost_Delete() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            Challenge challenge = getChallenge(null, member.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Post post = getPost(member, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            willReturn(Optional.empty()).given(postRepository).findById(1L);

            //when
            ResponseDto<?> responseDto = postService.delete(request, 1L);


            //then
            assertFalse(responseDto.isSuccess());
            assertEquals("게시물을 찾을 수 없습니다", responseDto.getData());
            verify(postRepository, never()).delete(post);
        }

        @Test
        @DisplayName("포스트 삭제 시, 일치하지 않는 사용자")
        void notMatchMember_Delete() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member postMember = getMember();
            Member requestMember = getMember();
            postMember.setId(3L);
            Challenge challenge = getChallenge(null, postMember.getId());
            ResponseDto<?> validateCheckResponse = ResponseDto.success(requestMember);
            Post post = getPost(postMember, challenge);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
            willReturn(Optional.of(post)).given(postRepository).findById(any());

            //when
            ResponseDto<?> responseDto = postService.delete(request, 1L);

            //then
            assertFalse(responseDto.isSuccess());
            assertEquals("해당 내역이 사용자의 내역이 아닙니다", responseDto.getData());
            verify(postRepository, never()).delete(post);
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

    private CreatePostServiceDto getCreateServiceDto() {
        List<String> imageUrlList = new ArrayList<>();
        imageUrlList.add("url1");
        imageUrlList.add("url2");

        return CreatePostServiceDto.builder()
                .content("내용")
                .imageUrlList(imageUrlList)
                .build();
    }

    private UpdatePostServiceDto getUpdateServiceDto() {
        return UpdatePostServiceDto.builder()
                .id(1L)
                .comment("수정내용")
                .build();
    }

    private List<Image> getImages() {
        return Arrays.asList(Image.builder()
                .id(1L)
                .postImage("ss.jpg")
                .build());
    }

    private Post getPost(Member member, Challenge challenge) {
        return Post.builder()
                .id(1L)
                .challenge(challenge)
                .member(member)
                .content("내용")
                .imageList(getImages())
                .build();
    }

    private List<Post> getPosts(int size, Member member, Challenge challenge) {
        List<Post> posts = new ArrayList<>();
        size = Math.max(size, 1);

        for (int i = size; i >= 1; i--) {
            Post post = Post.builder()
                    .id((long) i)
                    .challenge(challenge)
                    .member(member)
                    .imageList(getImages())
                    .content("내용")
                    .heartCnt(0)
                    .build();
            posts.add(post);
        }
        return posts;
    }

    private Challenge getChallenge(Category category, Long createMemberId) {
        return Challenge.builder()
                .id(1L)
                .masterMemberId(createMemberId)
                .title("title")
                .content("content")
                .startDate(LocalDate.of(2023, 7, 1))
                .endDate(LocalDate.of(2023, 8, 1))
                .goalAmount(300000)
                .category(category)
                .maxPeople(10)
                .build();
    }

    private Post getPost() {
        return Post.builder()
            .id(1L)
            .challenge(getChallenge(null, 1L))
            .member(getMember())
            .content("content")
            .imageList(getImages())
            .heartCnt(3)
            .build();
    }

}