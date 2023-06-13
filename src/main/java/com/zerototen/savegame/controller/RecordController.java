package com.zerototen.savegame.controller;

import com.zerototen.savegame.dto.CreateRecordForm;
import com.zerototen.savegame.dto.RecordResponse;
import com.zerototen.savegame.dto.UpdateRecordForm;
import com.zerototen.savegame.response.ResponseCode;
import com.zerototen.savegame.service.RecordService;
import com.zerototen.savegame.type.Category;
import com.zerototen.savegame.validation.EnumList;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/record")
public class RecordController {

    private final RecordService recordService;
//    private final JwtAuthenticationProvider provider;

    @PostMapping
    public ResponseEntity<?> createRecord(
//        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @RequestParam Long memberId, // 로그인 기능 구현 완료 시 위의 코드로 수정
        @RequestBody @Valid CreateRecordForm form) {
//        Long memberId = provider.getUserVo(accessToken).getId();
        recordService.create(form.toServiceDto(memberId));
        return ResponseEntity.status(ResponseCode.CREATE_SUCCESS.getStatus())
            .body(ResponseCode.CREATE_SUCCESS.getMessage());
    }

    @GetMapping
    public ResponseEntity<List<RecordResponse>> getInfos(
        @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
        @RequestParam(required = false) @Validated @EnumList(enumClass = Category.class, ignoreCase = true) List<String> categories,
//        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @RequestParam Long memberId) { // 로그인 기능 구현 완료 시 위의 코드로 수정

        return ResponseEntity.ok(recordService.getInfos(memberId, startDate, endDate, categories));
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateRecord(
//        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @PathVariable Long recordId,
        @RequestParam Long memberId, // 로그인 기능 구현 완료 시 위의 코드로 수정
        @RequestBody @Valid UpdateRecordForm form) {
//        Long memberId = provider.getUserVo(accessToken).getId();
        recordService.update(form.toServiceDto(recordId, memberId));
        return ResponseEntity.status(ResponseCode.UPDATE_SUCCESS.getStatus())
            .body(ResponseCode.UPDATE_SUCCESS.getMessage());
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteRecord(
//        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @PathVariable Long recordId,
        @RequestParam Long memberId) { // 로그인 기능 구현 완료 시 위의 코드로 수정)
//        Long memberId = provider.getUserVo(accessToken).getId();
        recordService.delete(recordId, memberId);
        return ResponseEntity.status(ResponseCode.DELETE_SUCCESS.getStatus())
            .body(ResponseCode.DELETE_SUCCESS.getMessage());
    }

}