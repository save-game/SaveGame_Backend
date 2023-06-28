package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.request.CreateChallengeRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.ChallengeService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    // 챌린지 생성
    @PostMapping
    public ResponseDto<?> create(HttpServletRequest request,
        @RequestBody @Valid CreateChallengeRequest createRequest) {
        return challengeService.create(request, CreateChallengeServiceDto.from(createRequest));
    }

    // 챌린지 참가
    @PostMapping("/join")
    public ResponseDto<?> join(HttpServletRequest request,
        @RequestParam Long challengeId) {
        return challengeService.join(request, challengeId);
    }

    // 챌린지 나가기
    @DeleteMapping("/{challengeId}")
    public ResponseDto<?> exit(HttpServletRequest request,
        @PathVariable Long challengeId) {
        return challengeService.exit(request, challengeId);
    }

}