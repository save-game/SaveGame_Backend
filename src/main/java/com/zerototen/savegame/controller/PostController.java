package com.zerototen.savegame.controller;


import com.zerototen.savegame.domain.dto.CreatePostServiceDto;
import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.request.CreatePostRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePostRequest;
import com.zerototen.savegame.domain.dto.response.PostResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.PostService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping
public class PostController {

    private final PostService postService;

    @GetMapping("/posts/challenges/{challengeId}")
    public ResponseDto<Page<PostResponse>> getPostList(
        HttpServletRequest request,
        @PathVariable Long challengeId,
        @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getPostList(request, challengeId, pageable);
    }

    @PostMapping("/posts")
    public ResponseDto<?> create(
        HttpServletRequest request,
        @RequestParam Long challengeId,
        @RequestBody @Valid CreatePostRequest postRequest) {
        return postService.create(CreatePostServiceDto.from(postRequest), challengeId, request);
    }

    @PutMapping("/posts/{postId}")
    public ResponseDto<?> update(
        HttpServletRequest request,
        @PathVariable Long postId,
        @RequestBody @Valid UpdatePostRequest updatePostRequest) {
        return postService.update(request, UpdatePostServiceDto.of(postId, updatePostRequest));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseDto<?> delete(HttpServletRequest request, @PathVariable Long postId) {
        return postService.delete(request, postId);
    }

}