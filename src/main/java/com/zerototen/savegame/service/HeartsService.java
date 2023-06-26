package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Hearts;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.HeartsRepository;
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
public class HeartsService {

    private final HeartsRepository heartsRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public ResponseDto<?> saveHearts(HttpServletRequest request, Long postId){
        Member member = validation(request);
        Post post = postRepository.findById(postId).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_POST));
        log.info("Create post -> memberId: {}", member.getId());

        // 연속 클릭시
        if(heartsRepository.existsByMember_IdAndPost_Id(member.getId(),post.getId())){
            heartsRepository.deleteByMemberAndPost(member,post);
            return ResponseDto.success("Unheart processed!");
        }
        heartsRepository.save(Hearts.from(member,post));
        return ResponseDto.success("Heart saved!");
    }

    @Transactional
    public ResponseDto<?> unHearts(HttpServletRequest request, Long postId){
        Member member = validation(request);
        Post post = postRepository.findById(postId).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_POST));

        // 연속 클릭시
        if(!heartsRepository.existsByMember_IdAndPost_Id(member.getId(),post.getId())){
            heartsRepository.save(Hearts.from(member,post));
            return ResponseDto.success("Heart saved!");
        }

        heartsRepository.deleteByMemberAndPost(member,post);
        log.info("Delete heart -> memberId: {}", member.getId());
        log.info("Delete heart -> id: {}", postId);
        return ResponseDto.success("Unheart processed!");
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
