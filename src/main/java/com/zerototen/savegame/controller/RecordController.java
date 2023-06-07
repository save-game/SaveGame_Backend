package com.zerototen.savegame.controller;

import com.zerototen.savegame.dto.CreateRecordForm;
import com.zerototen.savegame.response.ResponseCode;
import com.zerototen.savegame.service.RecordService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/record")
public class RecordController {

    private final RecordService recordService;
//    private final JwtAuthenticationProvider provider;

    @PostMapping
    public ResponseEntity<?> createRecord(
//        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @RequestParam Long id, // 로그인 기능 구현 완료 시 위의 코드로 수정
        @RequestBody @Valid CreateRecordForm form) {
//        Long id = provider.getUserVo(accessToken).getId();
        recordService.create(form.toServiceDto(id));
        return ResponseEntity.status(ResponseCode.CREATE_SUCCESS.getStatus())
            .body(ResponseCode.CREATE_SUCCESS.getMessage());
    }
}
