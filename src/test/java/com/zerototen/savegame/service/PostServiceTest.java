package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Heart;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.repository.HeartRepository;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
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
    private ImageService imageService;

    @Mock
    private HeartRepository heartRepository;

    @InjectMocks
    private PostService postService;


    @Test
    @Transactional
    @DisplayName("포스트 조회")
    void postList() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

        Pageable pageable = Pageable.ofSize(6);
        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
        willReturn(Optional.of(member))
            .given(memberRepository).findById(anyLong());

        Page<Post> postPage = new PageImpl<>(getPosts(13));
        willReturn(postPage)
            .given(postRepository).findByChallengeIdOrderByIdDesc(anyLong(),any());

        //when
        ResponseDto<?> responseDto = postService.getPostList(1L,request,pageable);
        Page<Post> all = postRepository.findByChallengeIdOrderByIdDesc(1L,pageable);

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals(13, all.getNumberOfElements());
        assertFalse(all.hasNext());
    }

    @Test
    @DisplayName("포스트 작성")
    void create() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        CreatePostServiceDto serviceDto = getCreateServiceDto();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));
        ArgumentCaptor<Post> argumentCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        ResponseDto<?> responseDto = postService.create(serviceDto, getStringImage(), request);

        //then
        assertTrue(responseDto.isSuccess());
        verify(postRepository).save(argumentCaptor.capture());
        Post saved = argumentCaptor.getValue();
        assertEquals(2L, saved.getMemberId());
        assertEquals(1L, saved.getChallengeId());
        assertEquals("내용", saved.getContent());
    }

    @Test
    @DisplayName("포스트 수정")
    void update() {
        //given
        UpdatePostServiceDto serviceDto = getUpdateServiceDto();
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
        Post post = getPost();

        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

        willReturn(Optional.of(post)).given(postRepository).findById(any());

        //when
        ResponseDto<?> responseDto = postService.update(request, serviceDto);

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals("Update Success", responseDto.getData());
        then(postRepository).should().findById(anyLong());
        assertEquals("수정완료", post.getContent());
    }

    @Test
    @DisplayName("포스트 삭제")
    void delete() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Member member = getMember();
        ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
        Post post = getPost();

        willReturn(validateCheckResponse)
            .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

        willReturn(Optional.of(post)).given(postRepository).findById(any());
        ArgumentCaptor<Post> argumentCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        ResponseDto<?> responseDto = postService.delete(request, 1L);

        //then
        assertTrue(responseDto.isSuccess());
        assertEquals("Delete Success", responseDto.getData());
        then(postRepository).should().findById(anyLong());
        then(postRepository).should().delete(argumentCaptor.capture());
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
        return CreatePostServiceDto.builder()
            .content("내용")
            .challengeId(1L)
            .build();
    }

    private UpdatePostServiceDto getUpdateServiceDto(){
        return UpdatePostServiceDto.builder()
            .id(1L)
            .comment("수정완료")
            .build();
    }

    private List<String> getStringImage(){
        return Arrays.asList("1.jpg", "2.jpg", "3.jpg");
    }

    private List<Image> getImages(){
        return Arrays.asList(Image.builder()
            .id(1L)
            .postImage("ss.jpg")
            .build());
    }

    private Post getPost() {
        return Post.builder()
            .id(1L)
            .memberId(2L)
            .content("내용")
            .imageList(getImages())
            .build();
    }

    private List<Heart> getHearts() {
        return new ArrayList<>();
    }

    private Pageable getPageable(){
        return Pageable.ofSize(12);
    }

    private List<Post> getPosts(int size){
        List<Post> posts = new ArrayList<>();
         size = Math.max(size, 1);

        for (int i = size; i >= 1; i--) {
            Post post = Post.builder()
                .id((long) i)
                .challengeId(1L)
                .memberId(2L)
                .imageList(getImages())
                .content("내용")
                .heartList(getHearts())
                .build();
           posts.add(post);
        }
        return posts;
    }
}