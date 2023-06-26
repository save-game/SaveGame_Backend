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
public class HeartsController {

    private final HeartsService heartsService;

    @PostMapping("/likes/{postId}")
    public ResponseDto<?> hearts(HttpServletRequest request, @PathVariable Long postId) {
        return heartsService.saveHearts(request, postId);
    }

    @DeleteMapping("/likes/{postId}")
    public ResponseDto<?> unHearts(HttpServletRequest request, @PathVariable Long postId){
        return heartsService.unHearts(request, postId);
    }

}
