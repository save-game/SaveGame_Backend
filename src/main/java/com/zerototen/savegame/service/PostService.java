package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.response.MemberResponse;
import com.zerototen.savegame.domain.dto.response.PostResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.repository.HeartRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final ImageService imageService;
    private final ChallengeRepository challengeRepository;
    private final HeartRepository heartRepository;

    @Transactional
    public ResponseDto<Page<PostResponse>> getPostList(HttpServletRequest request,
        Long challengeId, Pageable pageable) {
        Member member = validation(request);

        Page<Post> posts = postRepository.findByChallengeIdOrderByIdDesc(challengeId, pageable);

        Page<PostResponse> postResponses = posts.map(p -> from(p, member));

        return ResponseDto.success(postResponses);
    }

    private PostResponse from(Post post, Member member) {
        return PostResponse.builder()
            .id(post.getId())
            .challengeId(post.getChallenge().getId())
            .author(MemberResponse.from(post.getMember()))
            .postContent(post.getContent())
            .imageList(post.getImageList())
            .heartCnt(post.getHeartCnt())
            .hasHeart(heartRepository.existsByMemberAndPost(member, post))
            .createdAt(post.getCreatedAt())
            .build();
    }

    @Transactional
    public ResponseDto<String> create(CreatePostServiceDto serviceDto, Long challengeId, HttpServletRequest request) {
        Member member = validation(request);

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElse(null);
        if (challenge == null) {
            return ResponseDto.fail("챌린지가 존재하지 않습니다.");
        }

        log.info("Create Post");
        Post post = postRepository.save(Post.of(serviceDto, member, challenge));

        for (String image : serviceDto.getImageUrlList()) {
            Image img = new Image();
            img.setPostImage(image);
            img.setPost(post);
            imageService.save(img);
        }

        return ResponseDto.success("Post Create Success");
    }

    @Transactional
    public ResponseDto<?> update(HttpServletRequest request, UpdatePostServiceDto serviceDto) {
        Member member = validation(request);

        Post post = postRepository.findById(serviceDto.getId())
            .orElse(null);

        if (post == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_POST.getDetail());
        }

        if (!validateAuthority(post, member)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        post.update(serviceDto);
        postRepository.save(post);

        log.debug("Update Post -> postId: {}", serviceDto.getId());
        return ResponseDto.success("Post Update Success");
    }

    @Transactional
    public ResponseDto<String> delete(HttpServletRequest request, Long postId) {
        Member member = validation(request);
        Post post = postRepository.findById(postId)
            .orElse(null);

        if (post == null) {
            return ResponseDto.fail(ErrorCode.NOT_FOUND_POST.getDetail());
        }

        if (!validateAuthority(post, member)) {
            return ResponseDto.fail(ErrorCode.NOT_MATCH_MEMBER.getDetail());
        }

        postRepository.delete(post);
        log.info("Delete Post -> postId: {}", postId);
        return ResponseDto.success("Delete Success");
    }

    private boolean validateAuthority(Post post, Member member) {
        return post.getMember().getId().equals(member.getId());
    }

    public Member validation(HttpServletRequest request) {
        ResponseDto<?> responseDto = tokenProvider.validateCheck(request);

        if (!responseDto.isSuccess()) {
            throw new ValidationException("Validation failed.");
        }

        return (Member) responseDto.getData();
    }

}