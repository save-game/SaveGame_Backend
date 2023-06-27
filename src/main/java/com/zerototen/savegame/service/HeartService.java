package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Heart;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.HeartRepository;
import com.zerototen.savegame.repository.PostRepository;
import com.zerototen.savegame.security.TokenProvider;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeartService {

    private final HeartRepository heartRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public ResponseDto<?> create(HttpServletRequest request, Long postId){
        Member member = validation(request);
        Post post = postRepository.findById(postId).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_POST));

        // 연속 클릭시
        if (heartRepository.existsByMemberAndPost(member, post)) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_HEART);
        }

        heartRepository.save(Heart.of(member,post));
        log.info("Create heart -> memberId: {}, postId: {}", member.getId(), postId);
        return ResponseDto.success("Heart create success");
    }

    @Transactional
    public ResponseDto<?> delete(HttpServletRequest request, Long postId){
        Member member = validation(request);
        Post post = postRepository.findById(postId).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_POST));

        // 연속 클릭시
        if (!heartRepository.existsByMemberAndPost(member, post)) {
            throw new CustomException(ErrorCode.NOT_FOUND_HEART);
        }

        heartRepository.deleteByMemberAndPost(member,post);
        log.info("Delete heart -> memberId: {}, postId: {}", member.getId(), postId);
        return ResponseDto.success("Heart delete success");
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