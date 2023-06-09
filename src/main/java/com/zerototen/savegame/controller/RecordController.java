package com.zerototen.savegame.controller;

import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.dto.request.CreateRecordRequest;
import com.zerototen.savegame.domain.dto.request.UpdateRecordRequest;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.service.RecordService;
import com.zerototen.savegame.validation.EnumList;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/records")
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    public ResponseDto<?> create(
        HttpServletRequest request, @RequestBody @Valid CreateRecordRequest createRecordRequest) {
        return recordService.create(request, CreateRecordServiceDto.from(createRecordRequest));
    }

    @GetMapping
    public ResponseDto<?> getRecordList(
        HttpServletRequest request, @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(required = false) @Validated @EnumList(enumClass = Category.class, ignoreCase = true)
        List<String> categories) {
        return recordService.getRecordList(request, startDate, endDate, categories);
    }

    @GetMapping("/analysis")
    public ResponseDto<?> getRecordAnalysis(
        HttpServletRequest request, @RequestParam int year,
        @RequestParam @Valid @Min(1) @Max(12) int month) {
        return recordService.getRecordAnalysis(request, year, month);
    }

    @PutMapping("/{recordId}")
    public ResponseDto<?> update(
        HttpServletRequest request, @PathVariable Long recordId,
        @RequestBody @Valid UpdateRecordRequest updateRecordRequest) {
        return recordService.update(request,
            UpdateRecordServiceDto.of(recordId, updateRecordRequest));
    }

    @DeleteMapping("/{recordId}")
    public ResponseDto<?> delete(HttpServletRequest request, @PathVariable Long recordId) {
        return recordService.delete(request, recordId);
    }

}