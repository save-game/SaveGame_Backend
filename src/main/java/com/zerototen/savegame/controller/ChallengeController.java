package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.CreateChallengeServiceDto;
import com.zerototen.savegame.domain.dto.request.CreateChallengeRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.SearchType;
import com.zerototen.savegame.service.ChallengeService;
import com.zerototen.savegame.validation.Enum;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    private static final int PAGE_SIZE = 3; // 페이지 사이즈

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

    // 챌린지 검색
    // 로그인 안해도 가능
    @GetMapping("/search")
    public ResponseDto<?> getChallengeList(
        @RequestParam(required = false, defaultValue = "") String keyword,
        @RequestParam(required = false, defaultValue = "ALL") @Valid @Enum(enumClass = SearchType.class,
            ignoreCase = true, allowAll = true) String searchType,
        @RequestParam(required = false, defaultValue = "0") @Valid @Min(0) int minAmount,
        @RequestParam(required = false, defaultValue = "10000000") @Valid @Max(10000000) int maxAmount,
        @RequestParam(required = false) @Enum(enumClass = Category.class, ignoreCase = true,
            allowAll = true, nullable = true) String category,
        @RequestParam @Valid @Min(0) int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        return challengeService.getChallengeList(keyword, searchType, minAmount, maxAmount,
            category, pageable);
    }

}