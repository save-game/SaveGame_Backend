package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.CreateRecordForm;
import com.zerototen.savegame.domain.dto.RecordAnalysisResponse;
import com.zerototen.savegame.domain.dto.RecordResponse;
import com.zerototen.savegame.domain.dto.UpdateRecordForm;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.ResponseCode;
import com.zerototen.savegame.service.RecordService;
import com.zerototen.savegame.validation.EnumList;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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

    @PostMapping
    public ResponseEntity<?> createRecord(
//        @RequestHeader TODO: 토큰에 따라 ID 추출방식 바뀔 예정, 추가 시 memberId 부분을 이걸로 변경
        @RequestParam Long memberId,
        @RequestBody @Valid CreateRecordForm form) {
        recordService.create(form.toServiceDto(memberId));
        return ResponseEntity.status(ResponseCode.CREATE_SUCCESS.getStatus())
            .body(ResponseCode.CREATE_SUCCESS.getMessage());
    }

    @GetMapping
    public ResponseEntity<List<RecordResponse>> getInfos(
//        @RequestHeader TODO: 토큰에 따라 ID 추출방식 바뀔 예정, 추가 시 memberId 부분을 이걸로 변경
        @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
        @RequestParam(required = false) @Validated @EnumList(enumClass = Category.class, ignoreCase = true) List<String> categories,
        @RequestParam Long memberId) {
        return ResponseEntity.ok(recordService.getInfos(memberId, startDate, endDate, categories));
    }

    @GetMapping("/analysis")
    public ResponseEntity<List<RecordAnalysisResponse>> getAnalysisInfo(
//        @RequestHeader TODO: 토큰에 따라 ID 추출방식 바뀔 예정, 추가 시 memberId 부분을 이걸로 변경
        @RequestParam int year, @RequestParam @Valid @Min(1) @Max(12) int month,
        @RequestParam Long memberId) {
        return ResponseEntity.ok(recordService.getAnalysisInfo(memberId, year, month));
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateRecord(
//        @RequestHeader TODO: 토큰에 따라 ID 추출방식 바뀔 예정, 추가 시 memberId 부분을 이걸로 변경
        @PathVariable Long recordId,
        @RequestParam Long memberId,
        @RequestBody @Valid UpdateRecordForm form) {
        recordService.update(form.toServiceDto(recordId, memberId));
        return ResponseEntity.status(ResponseCode.UPDATE_SUCCESS.getStatus())
            .body(ResponseCode.UPDATE_SUCCESS.getMessage());
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteRecord(
//        @RequestHeader TODO: 토큰에 따라 ID 추출방식 바뀔 예정, 추가 시 memberId 부분을 이걸로 변경
        @PathVariable Long recordId,
        @RequestParam Long memberId) {
        recordService.delete(recordId, memberId);
        return ResponseEntity.status(ResponseCode.DELETE_SUCCESS.getStatus())
            .body(ResponseCode.DELETE_SUCCESS.getMessage());
    }

}