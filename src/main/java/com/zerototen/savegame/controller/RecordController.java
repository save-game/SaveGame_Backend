package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.request.CreateRecordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateRecordRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.security.TokenProvider;
import com.zerototen.savegame.service.RecordService;
import com.zerototen.savegame.validation.EnumList;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/record")
public class RecordController {

    private final RecordService recordService;
    private final TokenProvider tokenProvider;

    @PostMapping
    public ResponseDto<?> createRecord(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestBody @Valid CreateRecordRequest request) {

        return recordService.create(request.toServiceDto(tokenProvider.getMemberIdByToken(accessToken)));
    }

    @GetMapping
    public ResponseDto<?> getInfos(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
        @RequestParam(required = false) @Validated @EnumList(enumClass = Category.class, ignoreCase = true)
        List<String> categories) {
        return recordService.getInfos(tokenProvider.getMemberIdByToken(accessToken), startDate, endDate, categories);
    }

    @GetMapping("/analysis")
    public ResponseDto<?> getAnalysisInfo(
        @RequestHeader(name = "Authorization") String accessToken,
        @RequestParam int year, @RequestParam @Valid @Min(1) @Max(12) int month) {
        return recordService.getAnalysisInfo(tokenProvider.getMemberIdByToken(accessToken), year, month);
    }

    @PutMapping("/{recordId}")
    public ResponseDto<?> updateRecord(
        @RequestHeader(name = "Authorization") String accessToken,
        @PathVariable Long recordId,
        @RequestBody @Valid UpdateRecordRequest request) {

        return recordService.update(request.toServiceDto(recordId, tokenProvider.getMemberIdByToken(accessToken)));
    }

    @DeleteMapping("/{recordId}")
    public ResponseDto<?> deleteRecord(
        @RequestHeader(name = "Authorization") String accessToken,
        @PathVariable Long recordId) {

        return recordService.delete(recordId, tokenProvider.getMemberIdByToken(accessToken));
    }

}