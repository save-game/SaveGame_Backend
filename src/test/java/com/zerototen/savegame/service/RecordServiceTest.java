package com.zerototen.savegame.service;

import com.zerototen.savegame.dto.CreateRecordServiceDto;
import com.zerototen.savegame.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.entity.Record;
import com.zerototen.savegame.repository.RecordRepository;
import com.zerototen.savegame.type.Category;
import com.zerototen.savegame.type.PayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordService recordService;

    @Nested
    @DisplayName("지출 등록")
    class TestCreate {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            CreateRecordServiceDto serviceDto = CreateRecordServiceDto.builder()
                    .memberId(1L)
                    .amount(10000)
                    .category(Category.FOOD)
                    .paidFor("가게")
                    .memo("메모")
                    .useDate(LocalDate.of(2023, 1, 1))
                    .payType(PayType.CASH)
                    .build();

            // Login과 연동 시 memberId 검증 부분 추가

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            recordService.create(serviceDto);

            //then
            then(recordRepository).should().save(argumentCaptor.capture());

        }
        /* Login과 연동 시 추가
        @Test
        @DisplayName("지출 등록 실패 - 존재하지 않는 사용자")
        void fail_NotFoundUser() {
            //given

            //when

            //then
        }
        */
    }

    @Nested
    @DisplayName("지출 수정")
    class TestUpdate {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            UpdateRecordServiceDto serviceDto = UpdateRecordServiceDto.builder()
                    .id(1L)
                    .memberId(2L)
                    .amount(10000)
                    .category(Category.FOOD)
                    .paidFor("가게")
                    .memo("메모")
                    .useDate(LocalDate.of(2023, 1, 1))
                    .payType(PayType.CASH)
                    .build();

            Record record = Record.builder()
                    .id(1L)
                    .memberId(2L)
                    .amount(5000)
                    .category(Category.HEALTH)
                    .paidFor("병원")
                    .memo("메모메모")
                    .useDate(LocalDate.of(2023, 6, 6))
                    .payType(PayType.CARD)
                    .build();

            given(recordRepository.findById(anyLong())).willReturn(Optional.of(record));

            //when
            recordService.update(serviceDto);

            //then
            then(recordRepository).should().findById(anyLong());
            assertEquals(1L, record.getId());
            assertEquals(2L, record.getMemberId());
            assertEquals(10000, record.getAmount());
            assertEquals(Category.FOOD, record.getCategory());
            assertEquals("가게", record.getPaidFor());
            assertEquals("메모", record.getMemo());
            assertEquals(LocalDate.of(2023, 1, 1), record.getUseDate());
            assertEquals(PayType.CASH, record.getPayType());

        }

        @Nested
        @DisplayName("실패")
        class Fail {
            @Test
            @DisplayName("찾을 수 없는 지출내역")
            void notFoundRecord() {
                //given
                UpdateRecordServiceDto serviceDto = UpdateRecordServiceDto.builder()
                        .id(1L)
                        .memberId(2L)
                        .amount(10000)
                        .category(Category.FOOD)
                        .paidFor("가게")
                        .memo("메모")
                        .useDate(LocalDate.of(2023, 1, 1))
                        .payType(PayType.CASH)
                        .build();

                given(recordRepository.findById(anyLong())).willReturn(Optional.empty());

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> recordService.update(serviceDto));

                //then
                then(recordRepository).should().findById(anyLong());
                assertEquals("Not found record", exception.getMessage());

            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                UpdateRecordServiceDto serviceDto = UpdateRecordServiceDto.builder()
                        .id(1L)
                        .memberId(2L)
                        .amount(10000)
                        .category(Category.FOOD)
                        .paidFor("가게")
                        .memo("메모")
                        .useDate(LocalDate.of(2023, 1, 1))
                        .payType(PayType.CASH)
                        .build();

                given(recordRepository.findById(anyLong())).willReturn(Optional.of(Record.builder()
                        .id(1L)
                        .memberId(1L)
                        .amount(5000)
                        .category(Category.HEALTH)
                        .paidFor("병원")
                        .memo("메모메모")
                        .useDate(LocalDate.of(2023, 6, 6))
                        .payType(PayType.CARD)
                        .build()));

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> recordService.update(serviceDto));

                //then
                then(recordRepository).should().findById(anyLong());
                assertEquals("Not match member", exception.getMessage());

            }
        }
    }

    @Nested
    @DisplayName("지출 삭제")
    class TestDelete {
        @Test
        @DisplayName("성공")
        void success() {
            //given
            given(recordRepository.findById(anyLong())).willReturn(Optional.of(Record.builder()
                    .id(1L)
                    .memberId(2L)
                    .amount(10000)
                    .category(Category.FOOD)
                    .paidFor("가게")
                    .memo("메모")
                    .useDate(LocalDate.of(2023, 1, 1))
                    .payType(PayType.CASH)
                    .build()));

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            recordService.delete(1L, 2L);

            //then
            then(recordRepository).should().findById(anyLong());
            then(recordRepository).should().delete(argumentCaptor.capture());
        }

        @Nested
        @DisplayName("실패")
        class Fail {
            @Test
            @DisplayName("찾을 수 없는 지출내역")
            void notFoundRecord() {
                //given
                given(recordRepository.findById(anyLong())).willReturn(Optional.empty());

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> recordService.delete(1L, 2L));

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertEquals("Not found record", exception.getMessage());
            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                given(recordRepository.findById(anyLong())).willReturn(Optional.of(Record.builder()
                        .id(1L)
                        .memberId(2L)
                        .amount(10000)
                        .category(Category.FOOD)
                        .paidFor("가게")
                        .memo("메모")
                        .useDate(LocalDate.of(2023, 1, 1))
                        .payType(PayType.CASH)
                        .build()));

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> recordService.delete(1L, 1L));

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertEquals("Not match member", exception.getMessage());
            }
        }
    }

}