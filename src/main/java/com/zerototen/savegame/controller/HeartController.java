package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.service.HeartsService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class HeartController {

    private final HeartsService heartsService;

    @PostMapping("/heart/{postId}")
    public ResponseDto<?> create(HttpServletRequest request, @PathVariable Long postId) {
        return heartsService.create(request, postId);
    }

    @DeleteMapping("/heart/{postId}")
    public ResponseDto<?> delete(HttpServletRequest request, @PathVariable Long postId){
        return heartsService.delete(request, postId);
    }

}
