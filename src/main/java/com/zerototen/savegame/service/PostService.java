package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.response.PostResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Challenge;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.ChallengeRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final ImageService imageService;
    private final ChallengeRepository challengeRepository;

    @Transactional
    public ResponseDto<Page<PostResponse>> getPostList(HttpServletRequest request, Long challengeId,
        @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        validation(request);

        Page<Post> posts = postRepository.findByChallengeIdOrderByIdDesc(challengeId, pageable);

        return ResponseDto.success(posts.map(PostResponse::from));
    }

    @Transactional
    public ResponseDto<?> create(CreatePostServiceDto serviceDto, Long challengeId, HttpServletRequest request) {
        Member member = validation(request);

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElse(null);
        if (challenge == null) {
            ResponseDto.fail("챌린지가 존재하지 않습니다.");
        }

        log.info("Create Post");
        Post post = postRepository.save(Post.of(serviceDto, member, challenge));

        for (String image : serviceDto.getImageUrlList()) {
            Image img = new Image();
            img.setPostImage(image);
            img.setPost(post);
            imageService.save(img);
        }

        return ResponseDto.success(PostResponse.from(post));
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
        return ResponseDto.success(PostResponse.from(post));
    }

    @Transactional
    public ResponseDto<?> delete(HttpServletRequest request, Long postId) {
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