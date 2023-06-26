package com.zerototen.savegame.controller;


import com.zerototen.savegame.domain.dto.UpdatePostServiceDto;
import com.zerototen.savegame.domain.dto.request.CreatePostRequest;
import com.zerototen.savegame.domain.dto.request.UpdatePostRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.PostService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @GetMapping("/challenge/{challengeId}")
    public ResponseDto<?> challengePosts(HttpServletRequest request, @PathVariable Long challengeId,
    @PageableDefault(10) Pageable pageable){
        return postService.postList(challengeId, request, pageable);
    }

    @PostMapping
    public ResponseDto<?> post(HttpServletRequest request, @RequestParam List<String> urlImages,
        @RequestParam Long challengeId, @RequestBody @Valid CreatePostRequest postRequest) {
        // 이미지 이름 중복 없이 시분초+보드id로 작명될 수 있도록 요청 필요
        return postService.create(postRequest.toServiceDto(challengeId), urlImages, request);
    }

        //인스타그램 확인 시, 사진 수정/삭제 기능 없음, 필요하면 추가 필요
    @PutMapping("/{postId}")
    public ResponseDto<?> updatePost(HttpServletRequest request, @PathVariable Long postId,
        @RequestBody @Valid UpdatePostRequest updatePostRequest) {
        return postService.update(request, UpdatePostServiceDto.from(postId, updatePostRequest));
    }

    @DeleteMapping("/{postId}")
    public ResponseDto<?> deletePost(HttpServletRequest request, @PathVariable Long postId) {
        return postService.delete(request, postId);
    }

}
