package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.PostDetailDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.HeartRepository;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final ImageService imageService;
    private final MemberRepository memberRepository;
    private final HeartRepository heartRepository;

    @Transactional
    public ResponseDto<?> getPostList(Long challengeId, HttpServletRequest request, Pageable pageable){
        Member member = validation(request);
        log.info("check posts -> memberId: {}", member.getId());
        Page<Post> all = postRepository.findByChallengeIdOrderByIdDesc(challengeId, pageable);

        List<PostDetailDto> postDetailDtoList = new ArrayList<>();

        for(Post post:all){
            PostDetailDto postDetailDto = PostDetailDto.builder()
                .postId(post.getId())
                .nickname(memberRepository.findById(
                    post.getMemberId()).get().getNickname())
                .profileImage(memberRepository.findById(
                    post.getMemberId()).get().getProfileImageUrl())
                .content(post.getContent())
                .urlImages(post.getImageList().stream().map(Image::getPostImage)
                    .collect(Collectors.toList()))
                .heartCount(heartRepository.countByPost_Id(post.getId()))
                .heartState(heartRepository.existsByMemberAndPost(
                    member, post))
                .build();

            postDetailDtoList.add(postDetailDto);
        }

        return ResponseDto.success(new PageImpl<>(postDetailDtoList, pageable,
            all.getTotalElements()));

    }

    @Transactional
    public ResponseDto<?> create(CreatePostServiceDto serviceDto, List<String> imageList, HttpServletRequest request) {
        Member member = validation(request);
        log.info("Create post -> memberId: {}", member.getId());
        serviceDto.setMemberId(member.getId());
        Post post = postRepository.save(Post.from(serviceDto));

        for(String image : imageList){
            Image img = new Image();
            img.setPostImage(image);
            img.setPost(post);
            imageService.save(img);
        }

        return ResponseDto.success(post);
    }

    @Transactional
    public ResponseDto<?> update(HttpServletRequest request, UpdatePostServiceDto serviceDto) {
        Member member = validation(request);

        Post post = postRepository.findById(serviceDto.getId()).orElse(null);

        if (post == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_POST.getDetail());
        }

        if (!validateAuthority(post, member)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        post.update(serviceDto);

        log.debug("Update post -> id: {}", serviceDto.getId());
        return ResponseDto.success("Update Success");
    }

    @Transactional
    public ResponseDto<?> delete(HttpServletRequest request, Long postId) {
        Member member = validation(request);
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_POST.getDetail());
        }

        if (!validateAuthority(post, member)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        postRepository.delete(post);
        log.info("Delete post -> id: {}", postId);
        return ResponseDto.success("Delete Success");
    }

    private boolean validateAuthority(Post post, Member member) {
        return post.getMemberId().equals(member.getId());
    }

    public Member validation(HttpServletRequest request) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);

        if (!responseDto.isSuccess()) {
            throw new ValidationException("Validation failed.");
        }

        Member member = (Member) responseDto.getData();

        return member;
    }

}