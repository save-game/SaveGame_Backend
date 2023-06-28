package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.HeartService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hearts")
public class HeartController {

    private final HeartService heartsService;

    @PostMapping
    public ResponseDto<?> create(HttpServletRequest request, @RequestParam Long postId) {
        return heartsService.create(request, postId);
    }

    @DeleteMapping
    public ResponseDto<?> delete(HttpServletRequest request, @RequestParam Long postId){
        return heartsService.delete(request, postId);
    }

}